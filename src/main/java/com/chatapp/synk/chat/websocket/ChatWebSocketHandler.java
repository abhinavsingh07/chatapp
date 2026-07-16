package com.chatapp.synk.chat.websocket;

import com.chatapp.synk.chat.common.ChatMessage;
import com.chatapp.synk.chat.common.Json;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.chatapp.synk.chat.rabbitmq.ChatMessagePublisher;
import com.chatapp.synk.chat.redis.RedisSessionStore;
import com.chatapp.synk.enums.ChatWebSocketStatus;
import com.chatapp.synk.service.UserPresenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    private final LocalWsSessionRegistry localWsSessionRegistry;
    private final RedisSessionStore redisSessionStore;
    private final ChatMessagePublisher chatMessagePublisher;
    private final ExecutorService taskExecutor;
    private final UserPresenceService userPresenceService;

    public ChatWebSocketHandler(LocalWsSessionRegistry localWsSessionRegistry, RedisSessionStore redisSessionStore,
            ChatMessagePublisher chatMessagePublisher, ExecutorService taskExecutor,
            UserPresenceService userPresenceService) {
        this.localWsSessionRegistry = localWsSessionRegistry;
        this.redisSessionStore = redisSessionStore;
        this.chatMessagePublisher = chatMessagePublisher;
        this.taskExecutor = taskExecutor;
        this.userPresenceService = userPresenceService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession wsSession) throws Exception {
        String userId = (String) wsSession.getAttributes().get("userId");
        String sessionId = wsSession.getId();

        if (userId == null) {
            logger.warn("[WS_CONNECT_REJECTED] | sessionId={} reason=Missing userId", sessionId);
            wsSession.close(CloseStatus.BAD_DATA);
            return;
        }

        if (isDuplicateSession(userId, sessionId)) {
            closeSessionSafely(wsSession, CloseStatus.POLICY_VIOLATION.withReason("Duplicate session"),
                    userId, sessionId);
            return;
        }

        registerSession(userId, sessionId, wsSession);
        sendConnectedMessage(wsSession, userId);
    }

    @Override
    public void handleTextMessage(WebSocketSession wsSession, TextMessage message) throws Exception {
        String userId = (String) wsSession.getAttributes().get("userId");
        String sessionId = wsSession.getId();
        String payload = message.getPayload();

        logger.debug("[WS_MESSAGE_RECEIVED] | userId={} sessionId={} payload={}", userId, sessionId, payload);

        try {
            CompletableFuture.runAsync(() ->
                processInboundMessage(userId, sessionId, payload, wsSession), taskExecutor)
                .exceptionally(ex -> {
                    logger.error("[WS_ASYNC_TASK_FAILED] | userId={} sessionId={} payload={}",
                            userId, sessionId, payload, ex);
                    return null;
                });
        } catch (Exception e) {
            logger.error("[WS_TASK_SUBMISSION_FAILED] | userId={} sessionId={} payload={}",
                    userId, sessionId, payload, e);
            sendErrorMessage(wsSession, "Server error, please retry");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession wsSession, CloseStatus status) throws Exception {
        String userId = (String) wsSession.getAttributes().get("userId");
        String sessionId = wsSession.getId();

        if (userId != null) {
            try {
                userPresenceService.updateLastSeen(userId);
            } catch (Exception ex) {
                logger.error("[WS_LAST_SEEN_UPDATE_FAILED] | userId={} sessionId={}", userId, sessionId, ex);
            }
            localWsSessionRegistry.remove(userId);
            redisSessionStore.deleteUserSession(userId);
            logger.info("[WS_DISCONNECTED] | userId={} sessionId={} status={}", userId, sessionId, status);
        } else {
            logger.warn("[WS_DISCONNECTED_UNKNOWN] | sessionId={} status={}", sessionId, status);
        }

        try {
            if (wsSession.isOpen()) {
                wsSession.close(status);
            }
        } catch (Exception e) {
            logger.error("[WS_CLEANUP_FAILED] | userId={} sessionId={}", userId, sessionId, e);
        }
    }

    // -- private helpers ----------------------------------------------------

    private boolean isDuplicateSession(String userId, String sessionId) {
        WebSocketSession localSession = localWsSessionRegistry.get(userId);
        String storedRedisSessionId = redisSessionStore.getUserSessionId(userId);
        boolean hasLocalSession = (localSession != null && localSession.isOpen());
        boolean hasDifferentRedisSession = (storedRedisSessionId != null && !storedRedisSessionId.equals(sessionId));
        return hasLocalSession || hasDifferentRedisSession;
    }

    private void closeSessionSafely(WebSocketSession wsSession, CloseStatus reason,
            String userId, String sessionId) {
        logger.warn("[WS_CONNECT_REJECTED] | userId={} sessionId={} reason=Duplicate session", userId, sessionId);
        try {
            wsSession.close(reason);
        } catch (IOException e) {
            logger.error("[WS_CLOSE_FAILED] | userId={} sessionId={} error={}", userId, sessionId, e.getMessage(), e);
        }
    }

    private void registerSession(String userId, String sessionId, WebSocketSession wsSession) {
        String serverId = System.getProperty("server.id");
        localWsSessionRegistry.add(sessionId, userId, wsSession);
        redisSessionStore.saveUserSession(userId, serverId, sessionId);
        logger.info("[WS_CONNECTED] | userId={} sessionId={} serverId={}", userId, sessionId, serverId);
    }

    private void sendConnectedMessage(WebSocketSession wsSession, String userId) throws IOException {
        ObjectNode msg = Json.mapper().createObjectNode();
        msg.put("type", "connected");
        msg.put("userId", userId);
        msg.put("serverId", System.getProperty("server.id"));
        wsSession.sendMessage(new TextMessage(msg.toString()));
    }

    private void sendErrorMessage(WebSocketSession wsSession, String error) {
        try {
            ObjectNode msg = Json.mapper().createObjectNode();
            msg.put("error", error);
            wsSession.sendMessage(new TextMessage(msg.toString()));
        } catch (IOException e) {
            logger.error("[WS_ERROR_RESPONSE_FAILED] | sessionId={}", wsSession.getId(), e);
        }
    }

    private void processInboundMessage(String userId, String sessionId, String payload,
            WebSocketSession wsSession) {
        try {
            ChatMessage chatMessage = Json.mapper().readValue(payload, ChatMessage.class);

            if (ChatWebSocketStatus.HEARTBEAT.equals(chatMessage.getWsStatus())) {
                redisSessionStore.updateLastActiveTimestamp(userId);
                logger.debug("[WS_HEARTBEAT] | userId={} sessionId={}", userId, sessionId);
                return;
            }

            if (ChatWebSocketStatus.CHAT.equals(chatMessage.getWsStatus())) {
                chatMessage.setSentAt(Instant.now().toString());
                chatMessage.setFromUserId(userId);
            }

            chatMessagePublisher.sendToUser(chatMessage);
            logger.info("[WS_MESSAGE_PUBLISHED] | userId={} sessionId={} toUserId={}",
                    userId, sessionId, chatMessage.getToUserId());

        } catch (Exception ex) {
            logger.error("[WS_MESSAGE_PROCESSING_FAILED] | userId={} sessionId={} payload={}",
                    userId, sessionId, payload, ex);
            sendErrorMessage(wsSession, "Invalid message format or server error");
        }
    }
}
