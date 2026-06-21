package com.chatapp.synk.service.impl;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.chatapp.synk.dto.AuthDTO;
import com.chatapp.synk.dto.RefreshTokenDto;
import com.chatapp.synk.dto.RefreshTokenRequest;
import com.chatapp.synk.dto.UserDTO;
import com.chatapp.synk.entity.RefreshToken;
import com.chatapp.synk.entity.User;
import com.chatapp.synk.exceptionHandler.InvalidTokenException;
import com.chatapp.synk.exceptionHandler.ServiceException;
import com.chatapp.synk.repository.RefreshTokenRepository;
import com.chatapp.synk.repository.UserRepository;
import com.chatapp.synk.security.JwtResponse;
import com.chatapp.synk.security.JwtUtil;
import com.chatapp.synk.security_validator.InputSecurityUtils;
import com.chatapp.synk.security_validator.InputValidationAndSanitizationService;
import com.chatapp.synk.service.AuthService;
import com.chatapp.synk.service.UserService;
import com.chatapp.synk.util.HashUtil;
import com.chatapp.synk.util.Mapper;
import com.chatapp.synk.util.MaskIdentifierUtil;
import com.chatapp.synk.util.PasswordUtil;
import com.chatapp.synk.util.StringUtil;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    // Injects authentication dependencies used by this service.
    public AuthServiceImpl(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder, JwtUtil jwtUtil, UserService userService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    // Resets a user's password after validating the forgot-password request.
    @Override
    @Transactional
    public UserDTO forgotPassword(AuthDTO authDTO) {
        if (authDTO == null) {
            throw new ServiceException("Forgot password request data is required", HttpStatus.BAD_REQUEST);
        }

        String phoneNumberOrEmail = validateForgotPasswordIdentifier(authDTO.getPhoneNumberOrEmail());
        String newPassword = validateForgotPasswordPassword(authDTO.getPassword());

        if (!PasswordUtil.isStrongPassword(newPassword)) {
            throw new ServiceException(
                    "Password must be at least 8 characters and include uppercase, lowercase, and a digit",
                    HttpStatus.BAD_REQUEST);
        }
        // TODO: Validate OTP before allowing password reset.

        // TODO: Reject password reset when OTP is missing, expired, or already used.

        UserDTO existingUser = getUserForForgotPassword(phoneNumberOrEmail);
        User user = userRepository.findById(existingUser.getId())
                .orElseThrow(() -> new ServiceException("User not found with ID", HttpStatus.NOT_FOUND));

        user.setPassword(passwordEncoder.encode(newPassword));
        User updatedUser = userRepository.save(user);
        logger.info("Forgot password reset completed for user ID: {}", updatedUser.getId());

        UserDTO updatedUserDTO = Mapper.mapToUserDTO(updatedUser);
        updatedUserDTO.setPassword("********");
        return updatedUserDTO;
    }

    // Authenticates login credentials and returns access and refresh tokens.
    @Override
    public JwtResponse authenticate(AuthDTO authDTO) {
        if (logger.isDebugEnabled()) {
            logger.debug("Authentication request received for identifier: {}",
                    MaskIdentifierUtil.maskIdentifier(authDTO.getPhoneNumberOrEmail()));
        }

        AuthDTO sanitizedDTO = InputValidationAndSanitizationService.validateAndSanitize(authDTO);
        UserDTO user = authenticate(sanitizedDTO.getPhoneNumberOrEmail(), sanitizedDTO.getPassword());

        String role = user.getRoleName() != null ? user.getRoleName().name() : "";
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", role.isEmpty() ? List.of() : List.of(role));
        claims.put("id", user.getId());
        // dont store in jwt. In jwt only add important info like id, role etc.
        // even if user updates info we dont need to refresh jwt
        // user directly fetch latest details by id this is the main purpose to remove
        // other details
        // claims.put("email", user.getEmail());
        // claims.put("name", user.getName());//dont store in jwt
        //generate tokens
        String token = jwtUtil.generateAccessToken(claims, user.getPhoneNumber());
        String refreshToken = jwtUtil.generateRefreshToken(user.getPhoneNumber());
        // save refresh token on login
        RefreshTokenDto refreshTokenDto = buildRefreshTokenDto(refreshToken, user.getId());
        saveRefreshToken(refreshTokenDto);

        if (logger.isDebugEnabled()) {
            logger.debug("JWT token generated for user: {}", MaskIdentifierUtil.maskIdentifier(user.getPhoneNumber()));
        }

        return new JwtResponse(token, refreshToken, user.getEmail(), user.getName(),
                role, user.getEmail(), user.getProfilePictureUrl(), user.getId());
    }

    // Validates the username and password against the stored user credentials.
    private UserDTO authenticate(String username, String password) throws ServiceException {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Attempting authentication for user: {}", MaskIdentifierUtil.maskIdentifier(username));
            }

            UserDTO user = userService.getUserByPhoneNumberOrEmail(username);
            if (!passwordEncoder.matches(password, user.getPassword())) {
                logger.warn("Authentication failed - invalid credentials for user: {}",
                        MaskIdentifierUtil.maskIdentifier(username));
                throw new ServiceException("INVALID_CREDENTIALS", HttpStatus.UNAUTHORIZED);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Authentication successful for user: {}", MaskIdentifierUtil.maskIdentifier(username));
            }
            return user;
        } catch (ServiceException e) {
            logger.warn("Authentication failed - invalid credentials for user: {}",
                    MaskIdentifierUtil.maskIdentifier(username));
            throw new ServiceException("INVALID_CREDENTIALS", HttpStatus.UNAUTHORIZED);
        }
    }

    // Generates a new access token from a valid refresh token.
    @Override
    public JwtResponse refreshToken(RefreshTokenRequest request) {
        if (request == null || StringUtil.isBlank(request.getRefreshToken())) {
            throw new ServiceException("Refresh token is required", HttpStatus.BAD_REQUEST);
        }

        String refreshToken = request.getRefreshToken();
        RefreshToken storedRefreshToken = getStoredRefreshToken(refreshToken);
        if ('Y' == storedRefreshToken.isRevoked()) {
            throw new InvalidTokenException("Refresh token has been revoked");
        }
        // internally validating token signature
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
        logger.info("New JWT token generated via refresh for user ID: {}", user.getId());

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
    }
    // Loads the user referenced by a refresh token or raises an invalid-token
    // error.
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

    // Sanitizes the phone number or email used for forgot-password lookup.
    private String validateForgotPasswordIdentifier(String phoneNumberOrEmail) {
        try {
            return InputSecurityUtils.secureLoginId(phoneNumberOrEmail);
        } catch (SecurityException ex) {
            throw new ServiceException("Phone number or email must be valid", HttpStatus.BAD_REQUEST);
        }
    }

    // Sanitizes the replacement password for forgot-password requests.
    private String validateForgotPasswordPassword(String password) {
        try {
            return InputSecurityUtils.securePassword(password);
        } catch (SecurityException ex) {
            throw new ServiceException(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Loads the account for password reset and returns a clearer not-found message.
    private UserDTO getUserForForgotPassword(String phoneNumberOrEmail) {
        try {
            return userService.getUserByPhoneNumberOrEmail(phoneNumberOrEmail);
        } catch (ServiceException ex) {
            if (HttpStatus.NOT_FOUND.equals(ex.getStatus())) {
                throw new ServiceException("No account found for provided phone number or email", HttpStatus.NOT_FOUND);
            }
            throw ex;
        }
    }

    private RefreshTokenDto buildRefreshTokenDto(String refreshToken, String userId) {
        String hashedRefreshToken = HashUtil.hashWithSha256(refreshToken);
        Claims claims = jwtUtil.getTokenClaims(refreshToken);
        RefreshTokenDto refreshTokenDto = new RefreshTokenDto();
        refreshTokenDto.setUserId(userId);
        refreshTokenDto.setTokenHash(hashedRefreshToken);// always store hash of token in db
        refreshTokenDto.setIssuedAt(toInstant(claims.getIssuedAt()));
        refreshTokenDto.setExpiresAt(toInstant(claims.getExpiration()));
        refreshTokenDto.setRevoked('N');
        return refreshTokenDto;
    }

    private Instant toInstant(Date date) {
        return date != null ? date.toInstant() : null;
    }
}
