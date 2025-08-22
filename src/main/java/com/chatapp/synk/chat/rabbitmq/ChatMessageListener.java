package com.chatapp.synk.chat.rabbitmq;

import com.chatapp.synk.chat.common.DeliveryEnvelope;
import com.chatapp.synk.chat.common.Json;
import com.chatapp.synk.chat.redis.RedisSessionStore;
import com.chatapp.synk.chat.websocket.LocalWsSessionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Component
public class ChatMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(ChatMessageListener.class);

    private final LocalWsSessionRegistry localWsSessionRegistry;
    private final RedisSessionStore redisSessionStore;

    public ChatMessageListener(LocalWsSessionRegistry localWsSessionRegistry, RedisSessionStore redisSessionStore) {
        this.localWsSessionRegistry = localWsSessionRegistry;
        this.redisSessionStore = redisSessionStore;
    }

    @RabbitListener(queues = "#{serverQueue.name}")
    public void onMessage(String payload) {
        logger.debug("Received message payload: {}", payload);

        try {
            DeliveryEnvelope env = Json.mapper().readValue(payload, DeliveryEnvelope.class);
            logger.info("Parsed DeliveryEnvelope for targetUserId={} and targetSessionId={}", env.getTargetUserId(), env.getTargetSessionId());

            // 1. Try direct session first
            if (trySend(env.getTargetSessionId(), env)) {
                return;
            }

            // 2. Fallback: lookup fresh sessionId from Redis
            String freshSessionId = redisSessionStore.getUserSessionId(env.getTargetUserId());
            if (!trySend(freshSessionId, env)) {
                logger.warn("No active WebSocket session found for userId={}", env.getTargetUserId());
            }

        } catch (Exception e) {
            logger.error("Failed to process incoming message payload: {}", payload, e);
        }
    }

    /**
     * Attempts to send a message to a sessionId (if valid and open).
     *
     * @return true if sent successfully, false otherwise
     */
    private boolean trySend(String sessionId, DeliveryEnvelope env) {
        if (sessionId == null) {
            return false;
        }

        WebSocketSession session = localWsSessionRegistry.getWSSession(sessionId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(Json.mapper().writeValueAsString(env.getMessage())));
                logger.info("Delivered message to userId={} via sessionId={}", env.getTargetUserId(), sessionId);
                return true;
            } catch (Exception e) {
                logger.error("Failed to send message to userId={} via sessionId={}", env.getTargetUserId(), sessionId, e);
            }
        } else {
            logger.debug("SessionId={} not found or closed for userId={}", sessionId, env.getTargetUserId());
        }
        return false;
    }

}