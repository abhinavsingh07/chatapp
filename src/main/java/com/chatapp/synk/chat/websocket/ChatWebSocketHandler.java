package com.chatapp.synk.chat.websocket;

import com.chatapp.synk.chat.common.ChatMessage;
import com.chatapp.synk.chat.common.Json;
import com.chatapp.synk.chat.rabbitmq.ChatMessagePublisher;
import com.chatapp.synk.chat.redis.RedisSessionStore;
import com.chatapp.synk.dto.MessageDTO;
import com.chatapp.synk.enums.MessageStatus;
import com.chatapp.synk.enums.ChatWebSocketStatus;
import com.chatapp.synk.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    private final LocalWsSessionRegistry localWsSessionRegistry;//local session registery.
    private final RedisSessionStore redisSessionStore;//redis session store.
    private final ChatMessagePublisher chatMessagePublisher;
    private final MessageService messageService; // MessageService for additional message handling

    private final ExecutorService taskExecutor;

    // Constructor injection
    public ChatWebSocketHandler(LocalWsSessionRegistry localWsSessionRegistry, RedisSessionStore redisSessionStore, ChatMessagePublisher chatMessagePublisher, MessageService messageService, ExecutorService taskExecutor) {
        this.localWsSessionRegistry = localWsSessionRegistry;
        this.redisSessionStore = redisSessionStore;
        this.chatMessagePublisher = chatMessagePublisher;
        this.messageService = messageService;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession wsSession) throws Exception {
        String userId = (String) wsSession.getAttributes().get("userId");//contains the same map you populated in beforeHandshake().
        String serverId = System.getProperty("server.id");
        logger.info("afterConnectionEstablished userid:{}", userId);
        if (userId == null) {
            logger.warn("Connection rejected: no userId attribute found for sessionId={}", wsSession.getId());
            wsSession.close(CloseStatus.BAD_DATA);
            return;
        }

        localWsSessionRegistry.add(wsSession.getId(), userId, wsSession);//this for local lookup
        redisSessionStore.saveUserSession(userId, serverId, wsSession.getId());//stores serverId and sessionId in redis for a given userid.

        logger.info("WebSocket connection established for userId={} on serverId={}", userId, serverId);

        wsSession.sendMessage(new TextMessage("{\"type\":\"connected\",\"serverId\":\"" + serverId + "\"}"));
    }

    @Override
    public void handleTextMessage(WebSocketSession wsSession, TextMessage message) throws Exception {
        String userId = (String) wsSession.getAttributes().get("userId");//contains the same map you populated in beforeHandshake().
        String payload = message.getPayload();

        logger.info("Received message from sessionId={} userId={} -> payload={}", wsSession.getId(), userId, payload);
        //we should cover this code in async task to dont block event loop of websocket which is using under the hood of websocket.
        try {
            // Send invite asynchronously; it is non-blocking to WebSocket event loop under the hood.
            CompletableFuture.runAsync(() -> {
                ChatMessage chatMessage;
                try {
                    // Parse JSON payload into ChatMessage
                    chatMessage = Json.mapper().readValue(payload, ChatMessage.class);

                    // Only process if the message is of type CHAT
                    if (chatMessage.getWsStatus().equals(ChatWebSocketStatus.CHAT)) {
                        // Ensure 'fromUserId' matches the authenticated user
                        chatMessage.setFromUserId(userId);

                        // Convert chatMessage to MessageDTO
                        MessageDTO messageDTO = new MessageDTO();
                        messageDTO.setSenderId(chatMessage.getFromUserId());
                        messageDTO.setReceiverId(chatMessage.getToUserId());
                        messageDTO.setContent(chatMessage.getBody());
                        messageDTO.setConversationId(chatMessage.getConversationId());
                        messageDTO.setMessageStatus(MessageStatus.SENT);

                        // Save message
                        messageService.createMessage(messageDTO);
                        logger.info("Chat message saved.");
                    }

                    // Hand over to publisher for routing via RabbitMQ
                    chatMessagePublisher.sendToUser(chatMessage);

                } catch (Exception ex) {
                    // Exceptions inside async block must be handled here
                    logger.error("Async task failed for userId={} payload={}", userId, payload, ex);
                    try {
                        wsSession.sendMessage(new TextMessage("{\"error\":\"Invalid message format or server error\"}"));
                    } catch (IOException ioEx) {
                        logger.error("Failed to send error message to client", ioEx);
                    }
                }
            }, taskExecutor).exceptionally(ex -> {
                // This ensures unexpected runtime exceptions are still logged
                logger.error("Unhandled async exception for userId={} payload={}", userId, payload, ex);
                return null;
            });

        } catch (Exception e) {
            // This only catches exceptions thrown before async execution is submitted
            logger.error("Failed to submit async task for userId={} payload={}", userId, payload, e);
            wsSession.sendMessage(new TextMessage("{\"error\":\"Server error, please retry\"}"));
        }

    }

    @Override
    //when client calls socket.close()
    public void afterConnectionClosed(WebSocketSession wsSession, CloseStatus status) throws Exception {
        String userId = (String) wsSession.getAttributes().get("userId");

        if (userId != null) {
            // Cleanup local and distributed registries
            localWsSessionRegistry.remove(userId);
            redisSessionStore.deleteUserSession(userId);
            logger.info("WebSocket connection closed for userId={} sessionId={} with status={}", userId, wsSession.getId(), status);
        } else {
            // Defensive: session had no userId
            logger.warn("WebSocket connection closed for unknown user, sessionId={} with status={}", wsSession.getId(), status);
        }

        try {
            // Ensure session is closed at the transport level
            if (wsSession.isOpen()) {
                wsSession.close(status);
            }
        } catch (Exception e) {
            logger.error("Error during cleanup of sessionId={} userId={}", wsSession.getId(), userId, e);
        }
    }
}