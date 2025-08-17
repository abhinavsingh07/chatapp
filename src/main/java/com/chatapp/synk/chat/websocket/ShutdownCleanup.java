package com.chatapp.synk.chat.websocket;

import com.chatapp.synk.chat.redis.RedisSessionStore;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ShutdownCleanup {

    private static final Logger logger = LoggerFactory.getLogger(ShutdownCleanup.class);

    private final LocalWsSessionRegistry localRegistry;
    private final RedisSessionStore sessionStore;

    public ShutdownCleanup(LocalWsSessionRegistry localRegistry, RedisSessionStore sessionStore) {
        this.localRegistry = localRegistry;
        this.sessionStore = sessionStore;
    }

    @PreDestroy
    public void onShutdown() {
        logger.info("Shutdown initiated. Cleaning up {} sessions from Redis.", localRegistry.entries().size());

        for (Map.Entry<String, String> e : localRegistry.entries()) {
            String sessionId = e.getKey();
            String userId = e.getValue();

            try {
                sessionStore.deleteUserSession(userId);
                logger.info("Cleaned up sessionId={} for userId={} from Redis.", sessionId, userId);
            } catch (Exception ex) {
                logger.error("Failed to clean up sessionId={} for userId={}", sessionId, userId, ex);
            }
        }

        logger.info("Shutdown cleanup completed.");
    }
}