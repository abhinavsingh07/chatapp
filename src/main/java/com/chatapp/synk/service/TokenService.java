package com.chatapp.synk.service;

import com.chatapp.synk.dto.RefreshTokenDto;
import com.chatapp.synk.dto.RefreshTokenRequest;
import com.chatapp.synk.security.JwtResponse;

/**
 * Manages JWT refresh token lifecycle — generation, storage, refresh, and revocation.
 * Separated from {@link AuthService} to keep authentication and token management
 * as distinct concerns (SRP).
 */
public interface TokenService {

    /**
     * Generate a new access token from a valid refresh token.
     */
    JwtResponse refreshToken(RefreshTokenRequest request);

    /**
     * Persist a new refresh token hash in the database.
     */
    RefreshTokenDto saveRefreshToken(RefreshTokenDto refreshTokenDto);

    /**
     * Revoke a refresh token so it can no longer be used.
     */
    void revokeTokenMethod(RefreshTokenRequest request);
}
