package com.chatapp.synk.chat.websocket;

import com.chatapp.synk.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Component
public class WebSocketAuthHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketAuthHandshakeInterceptor.class);
    private final JwtUtil jwtUtil;

    public WebSocketAuthHandshakeInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        logger.info("Incoming WebSocket handshake: URI={}", request.getURI());

        // 1) Try Authorization: Bearer <token>
        String token = null;
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            logger.debug("Extracted token from Authorization header.");
        }

        // 2) Fallback to query param ?token=...
        if (!StringUtils.hasText(token)) {
            var uri = request.getURI();
            var params = UriComponentsBuilder.fromUri(uri).build().getQueryParams();
            token = params.getFirst("token");
            if (StringUtils.hasText(token)) {
                logger.debug("Extracted token from query parameters.");
            }
        }

        // 3) Fallback to Read JWT from cookie
        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            Cookie[] cookies = servletRequest.getCookies();

            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("jwt".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }
        }

        // 3) Validate
        if (!StringUtils.hasText(token)) {
            logger.warn("Handshake rejected: No token provided.");
            return false;
        }

        if (!jwtUtil.isTokenValid(token)) {
            logger.warn("Handshake rejected: Invalid token.");
            return false;
        }

        // 4) Extract user id from token and store for WebSocket handler
        String userId = jwtUtil.extractId(token);
        attributes.put("userId", userId);//store in WebSocketSession,In ChatWebSocketHandler it is using as session.getAttributes().get("userId");
        attributes.put("authToken", token);//store in WebSocketSession,example in ChatWebSocketHandler it is fetching userid from ws session it remians for whole session
        logger.info("Handshake authorized: userId={} connected successfully.", userId);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            logger.error("Error occurred after WebSocket handshake: {}", exception.getMessage(), exception);
        } else {
            logger.debug("AfterHandshake completed without errors for URI={}", request.getURI());
        }
    }
}