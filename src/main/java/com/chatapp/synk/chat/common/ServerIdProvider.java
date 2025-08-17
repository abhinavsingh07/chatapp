package com.chatapp.synk.chat.common;

import com.chatapp.synk.chat.rabbitmq.ChatMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class ServerIdProvider {
    private static final Logger logger = LoggerFactory.getLogger(ChatMessageListener.class);
    private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int LENGTH = 5;
    private static String serverId;

    public ServerIdProvider() {
        this.serverId = generateRandomId();
        System.setProperty("server.id", serverId); //make accessible globally
        logger.info("Generated serverId: {}", serverId);
    }

    private String generateRandomId() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(CHARSET.charAt(random.nextInt(CHARSET.length())));
        }
        return sb.toString();
    }

    public static String getServerId() {
        return serverId;
    }
}