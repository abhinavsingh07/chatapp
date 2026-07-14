package com.chatapp.synk.security;

import java.util.List;

/**
 * Centralized security constants — single source of truth for public endpoints.
 * Both {@link SecurityConfig} and {@link JwtAuthFilter} reference these lists
 * so they never go out of sync.
 */
public final class SecurityConstants {

    private SecurityConstants() {
        // utility class — prevent instantiation
    }

    /** Auth endpoints that require no JWT validation. */
    public static final List<String> PUBLIC_AUTH_URLS = List.of(
            "/auth/authenticate",
            "/auth/register",
            "/auth/refresh",
            "/auth/logout",
            "/auth/forgot-password");

    /** Static resources and infrastructure endpoints. */
    public static final String[] PUBLIC_STATIC_RESOURCES = {
            "/ws/chat",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/**"
    };
}
