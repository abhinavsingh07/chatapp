package com.chatapp.synk.chat.rabbitmq;

import com.chatapp.synk.chat.common.ChatMessage;
import com.chatapp.synk.chat.common.DeliveryEnvelope;
import com.chatapp.synk.chat.common.Json;
import com.chatapp.synk.chat.redis.RedisSessionStore;
import com.chatapp.synk.chat.websocket.LocalWsSessionRegistry;
import com.chatapp.synk.dto.MessageDTO;
import com.chatapp.synk.enums.ChatWebSocketStatus;
import com.chatapp.synk.enums.MessageStatus;
import com.chatapp.synk.service.MessageService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Component
public class ChatMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(ChatMessageListener.class);

    private final MessageService messageService;
    private final LocalWsSessionRegistry localWsSessionRegistry;
    private final RedisSessionStore redisSessionStore;

    public ChatMessageListener(MessageService messageService, LocalWsSessionRegistry localWsSessionRegistry, 
            RedisSessionStore redisSessionStore) {
        this.messageService = messageService;
        this.localWsSessionRegistry = localWsSessionRegistry;
        this.redisSessionStore = redisSessionStore;
    }

    @RabbitListener(queues = "#{serverQueue.name}")
    public void onMessage(String payload, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        logger.debug("[RabbitMQ] Received raw payload: {}", payload);

        try {
            DeliveryEnvelope deliveryEnvelope = Json.mapper().readValue(payload, DeliveryEnvelope.class);
            createInfoLog("[RabbitMQ] Converted payload into DeliveryEnvelope conversationId={} targetUserId={}", deliveryEnvelope.getMessage().getConversationId(), deliveryEnvelope.getTargetUserId(), deliveryEnvelope.getMessage());

            // Persist only chat messages with atomicity guarantee
            if (deliveryEnvelope.getMessage().getWsStatus().equals(ChatWebSocketStatus.CHAT)) {
                MessageDTO messageDTO = new MessageDTO();
                messageDTO.setSenderId(deliveryEnvelope.getMessage().getFromUserId());
                messageDTO.setReceiverId(deliveryEnvelope.getMessage().getToUserId());
                messageDTO.setContent(deliveryEnvelope.getMessage().getBody());
                messageDTO.setConversationId(deliveryEnvelope.getMessage().getConversationId());
                messageDTO.setMessageStatus(MessageStatus.SENT);

                // Atomically save message and update media IDs (single transaction)
                String mediaIdsStr = deliveryEnvelope.getMessage().getMediaIds();
                logger.info("[DB] MEDIA STRR..**** {}", mediaIdsStr);
                //db call
                messageService.saveMessageWithMediaIds(messageDTO, mediaIdsStr, 
                    Long.valueOf(deliveryEnvelope.getMessage().getFromUserId()));
                logger.info("[DB] Persisted message with media associations conversationId={} senderId={} receiverId={}", 
                    deliveryEnvelope.getMessage().getConversationId(), deliveryEnvelope.getMessage().getFromUserId(), 
                    deliveryEnvelope.getMessage().getToUserId());
            }

            // Attempt delivery
            if (!trySend(deliveryEnvelope.getTargetSessionId(), deliveryEnvelope)) {
                logger.debug("[WS] SessionId={} not active, checking Redis...", deliveryEnvelope.getTargetSessionId());

                String freshSessionId = redisSessionStore.getUserSessionId(deliveryEnvelope.getTargetUserId());
                if (!trySend(freshSessionId, deliveryEnvelope)) {
                    logger.warn("[WS] No active WebSocket session for userId={}", deliveryEnvelope.getTargetUserId());
                }
            }

            // Acknowledge after DB + delivery attempt
            channel.basicAck(tag, false);
            createInfoLog("[RabbitMQ] Message acked conversationId={} userId={}", deliveryEnvelope.getMessage().getConversationId(), deliveryEnvelope.getTargetUserId(), deliveryEnvelope.getMessage());

        } catch (Exception e) {
            logger.error("[RabbitMQ] Failed processing payload (hash={}): {}", payload.hashCode(), e.getMessage(), e);
            try {
                channel.basicNack(tag, false, true);
                logger.warn("[RabbitMQ] Message nacked & requeued (hash={})", payload.hashCode());
            } catch (IOException ioEx) {
                logger.error("[RabbitMQ] Failed to nack message (hash={})", payload.hashCode(), ioEx);
            }
        }
    }

    /**
     * Attempts to send a message to a WebSocket session.
     */
    private boolean trySend(String sessionId, DeliveryEnvelope env) {
        if (sessionId == null) {
            logger.debug("[WS] No sessionId for userId={}", env.getTargetUserId());
            return false;
        }

        //websocket is maintaing in localWsSessionRegistry of each server 
        WebSocketSession wsSession = localWsSessionRegistry.getWSSession(sessionId);
        if (wsSession != null && wsSession.isOpen()) {
            try {
                wsSession.sendMessage(new TextMessage(Json.mapper().writeValueAsString(env.getMessage())));
                createInfoLog("[WS] Delivered to userId={} sessionId={}", env.getTargetUserId(), sessionId, env.getMessage());
                return true;
            } catch (Exception e) {
                logger.error("[WS] Failed delivery userId={} sessionId={} error={}", env.getTargetUserId(), sessionId, e.getMessage());
            }
        } else {
            logger.debug("[WS] SessionId={} closed/missing for userId={}", sessionId, env.getTargetUserId());
        }
        return false;
    }

    /**
     * Info logs only for chat messages, avoids clutter for typing/other statuses.
     */
    private void createInfoLog(String template, Object... args) {
        ChatMessage chatMessage = (ChatMessage) args[args.length - 1];
        if (chatMessage.getWsStatus().equals(ChatWebSocketStatus.CHAT)) {
            logger.info(template, args);
        }
    }
}

