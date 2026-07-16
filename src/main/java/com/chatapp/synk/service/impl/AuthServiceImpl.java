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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.chatapp.synk.dto.AuthDTO;
import com.chatapp.synk.dto.RefreshTokenDto;
import com.chatapp.synk.dto.UserDTO;
import com.chatapp.synk.entity.User;
import com.chatapp.synk.exceptionHandler.ServiceException;
import com.chatapp.synk.repository.UserRepository;
import com.chatapp.synk.security.CustomUserDetails;
import com.chatapp.synk.security.JwtResponse;
import com.chatapp.synk.security.JwtUtil;
import com.chatapp.synk.security.PhoneNumberAuthenticationToken;
import com.chatapp.synk.security_validator.InputSecurityUtils;
import com.chatapp.synk.security_validator.InputValidationAndSanitizationService;
import com.chatapp.synk.service.AuthService;
import com.chatapp.synk.service.TokenService;
import com.chatapp.synk.service.UserService;
import com.chatapp.synk.util.HashUtil;
import com.chatapp.synk.util.Mapper;
import com.chatapp.synk.util.MaskIdentifierUtil;
import com.chatapp.synk.util.PasswordUtil;

@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public AuthServiceImpl(UserRepository userRepository,
            PasswordEncoder passwordEncoder, JwtUtil jwtUtil, UserService userService,
            AuthenticationManager authenticationManager, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    // Resets a user's password after validating the forgot-password request.
    @Override
    @Transactional
    public UserDTO forgotPassword(AuthDTO authDTO) {
        if (logger.isDebugEnabled()) {
            logger.debug("Forogt Password request received for phoenumber: {}",
                    MaskIdentifierUtil.maskIdentifier(authDTO.getPhoneNumberOrEmail()));
        }

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
        User user = userRepository.findById(Long.parseLong(existingUser.getId()))
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

        Authentication auth = authenticate(sanitizedDTO.getPhoneNumberOrEmail(), sanitizedDTO.getPassword());
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();

        String role = user.getUserRoles() != null ? user.getUserRoles() : "";
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", role.isEmpty() ? List.of() : List.of(role));
        claims.put("id", user.getId());
        // dont store in jwt. In jwt only add important info like id, role etc.
        // even if user updates info we dont need to refresh jwt
        // user directly fetch latest details by id this is the main purpose to remove
        // other details
        // claims.put("email", user.getEmail());
        // claims.put("name", user.getName());//dont store in jwt
        // generate tokens
        String token = jwtUtil.generateAccessToken(claims, user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
        // save refresh token on login
        RefreshTokenDto refreshTokenDto = buildRefreshTokenDto(refreshToken, user.getId());
        tokenService.saveRefreshToken(refreshTokenDto);

        if (logger.isDebugEnabled()) {
            logger.debug("JWT token generated for user: {}", MaskIdentifierUtil.maskIdentifier(user.getUsername()));
        }

        return new JwtResponse(token, refreshToken, user.getEmail(), user.getName(),
                role, user.getEmail(), user.getProfilePictureUrl(), user.getId());
    }

    // Validates the username and password against the stored user credentials.
    private Authentication authenticate(String username, String password) throws ServiceException {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Attempting authentication for user: {}", MaskIdentifierUtil.maskIdentifier(username));
            }

            Authentication auth = authenticationManager
                    .authenticate(new PhoneNumberAuthenticationToken(username, password));

            if (logger.isDebugEnabled()) {
                logger.debug("Authentication successful for user: {}", MaskIdentifierUtil.maskIdentifier(username));
            }
            return auth;
        } catch (DisabledException e) {
            logger.warn("Authentication failed - account disabled for user: {}",
                    MaskIdentifierUtil.maskIdentifier(username));
            throw new ServiceException("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            logger.warn("Authentication failed - invalid credentials for user: {}",
                    MaskIdentifierUtil.maskIdentifier(username));
            throw new ServiceException("INVALID_CREDENTIALS", e);
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
