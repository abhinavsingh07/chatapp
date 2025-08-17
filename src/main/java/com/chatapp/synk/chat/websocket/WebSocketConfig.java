package com.chatapp.synk.chat.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    private final ChatWebSocketHandler chatHandler;
    private final WebSocketAuthHandshakeInterceptor authInterceptor;

    public WebSocketConfig(ChatWebSocketHandler chatHandler, WebSocketAuthHandshakeInterceptor authInterceptor) {
        this.chatHandler = chatHandler;
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        logger.info("Registering WebSocket handler at endpoint: /ws/chat");
        registry.addHandler(chatHandler, "/ws/chat")
                .addInterceptors(authInterceptor)
                .setAllowedOrigins("*");
        logger.debug("WebSocket handler [{}] with interceptor [{}] successfully registered.",
                chatHandler.getClass().getSimpleName(),
                authInterceptor.getClass().getSimpleName());
    }
}