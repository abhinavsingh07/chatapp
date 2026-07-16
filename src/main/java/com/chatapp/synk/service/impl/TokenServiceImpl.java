package com.chatapp.synk.service.impl;

import com.chatapp.synk.dto.RefreshTokenDto;
import com.chatapp.synk.dto.RefreshTokenRequest;
import com.chatapp.synk.dto.UserDTO;
import com.chatapp.synk.entity.RefreshToken;
import com.chatapp.synk.exceptionHandler.InvalidTokenException;
import com.chatapp.synk.exceptionHandler.ServiceException;
import com.chatapp.synk.repository.RefreshTokenRepository;
import com.chatapp.synk.security.JwtResponse;
import com.chatapp.synk.security.JwtUtil;
import com.chatapp.synk.service.TokenService;
import com.chatapp.synk.service.UserService;
import com.chatapp.synk.util.HashUtil;
import com.chatapp.synk.util.Mapper;
import com.chatapp.synk.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TokenServiceImpl implements TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenServiceImpl.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public TokenServiceImpl(RefreshTokenRepository refreshTokenRepository,
            JwtUtil jwtUtil,
            UserService userService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Override
    public JwtResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        if (logger.isDebugEnabled()) {
            logger.debug("Generating new access token via refresh token");
        }

        if (refreshTokenRequest == null || StringUtil.isBlank(refreshTokenRequest.getRefreshToken())) {
            throw new ServiceException("Refresh token is required", HttpStatus.BAD_REQUEST);
        }

        String refreshToken = refreshTokenRequest.getRefreshToken();
        RefreshToken storedRefreshToken = getStoredRefreshToken(refreshToken);
        if ('Y' == storedRefreshToken.isRevoked()) {
            throw new InvalidTokenException("Refresh token has been revoked");
        }

        String username = jwtUtil.extractUsername(refreshToken);
        if (StringUtil.isBlank(username)) {
            throw new InvalidTokenException("Refresh token validation failed - username not found");
        }

        UserDTO user = getUserForRefreshToken(username);
        String role = user.getRoleName() != null ? user.getRoleName().name() : "";

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", role.isEmpty() ? List.of() : List.of(role));
        claims.put("id", user.getId());

        String newToken = jwtUtil.generateAccessToken(claims, user.getPhoneNumber());

        return new JwtResponse(newToken, refreshToken, user.getEmail(), user.getName(),
                role, user.getEmail(), user.getProfilePictureUrl(), user.getId());
    }

    @Override
    @Transactional
    public RefreshTokenDto saveRefreshToken(RefreshTokenDto refreshTokenDto) {
        if (refreshTokenDto == null) {
            throw new ServiceException("Refresh token data is required", HttpStatus.BAD_REQUEST);
        }
        if (StringUtil.isBlank(refreshTokenDto.getUserId())) {
            throw new ServiceException("Refresh token user ID is required", HttpStatus.BAD_REQUEST);
        }
        if (StringUtil.isBlank(refreshTokenDto.getTokenHash())) {
            throw new ServiceException("Refresh token hash is required", HttpStatus.BAD_REQUEST);
        }
        if (refreshTokenDto.getExpiresAt() == null) {
            throw new ServiceException("Refresh token expiry is required", HttpStatus.BAD_REQUEST);
        }

        RefreshToken refreshToken = Mapper.mapToRefreshTokenEntity(refreshTokenDto);
        RefreshToken savedRefreshToken = refreshTokenRepository.save(refreshToken);
        return Mapper.mapToRefreshTokenDto(savedRefreshToken);
    }

    @Override
    @Transactional
    public void revokeTokenMethod(RefreshTokenRequest request) {
        if (request == null || StringUtil.isBlank(request.getRefreshToken())) {
            throw new ServiceException("Refresh token is required", HttpStatus.BAD_REQUEST);
        }

        String tokenHash = HashUtil.hashWithSha256(request.getRefreshToken());
        int revokedCount = refreshTokenRepository.revokeToken(tokenHash);
        if (revokedCount == 0) {
            throw new ServiceException("Refresh token not found", HttpStatus.NOT_FOUND);
        }
        logger.info("Refresh token revoked successfully.");
    }

    private RefreshToken getStoredRefreshToken(String refreshToken) {
        String tokenHash = HashUtil.hashWithSha256(refreshToken);
        return refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Refresh token validation failed - token not found"));
    }

    private UserDTO getUserForRefreshToken(String username) {
        try {
            return userService.getUserByPhoneNumberOrEmail(username);
        } catch (ServiceException ex) {
            throw new InvalidTokenException("Refresh token validation failed - user not found", ex);
        }
    }
}
