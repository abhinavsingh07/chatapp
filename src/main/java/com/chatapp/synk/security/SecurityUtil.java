package com.chatapp.synk.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static String getCurrentUserIdFromSecurityContext() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof String userId) {
            return userId;
        }

        throw new AccessDeniedException("Unable to determine current user");
    }
}