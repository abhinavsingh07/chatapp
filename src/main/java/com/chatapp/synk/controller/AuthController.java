package com.chatapp.synk.controller;

import com.chatapp.synk.dto.AuthDTO;
import com.chatapp.synk.dto.UserDTO;
import com.chatapp.synk.exceptionHandler.ServiceException;
import com.chatapp.synk.security.JwtResponse;
import com.chatapp.synk.response.SuccessResponse;
import com.chatapp.synk.security.CustomUserDetailsService;
import com.chatapp.synk.security.PhoneNumberAuthenticationToken;
import com.chatapp.synk.service.UserService;
import com.chatapp.synk.util.JwtUtil;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private UserService userService;

    @PostMapping("/authenticate")
    public ResponseEntity<JwtResponse> authenticate(@RequestBody AuthDTO request) throws ServiceException {
        logger.info("Authenticating user with phone number: {}", request.getPhoneNumber());

        authenticate(request.getPhoneNumber());

        //token generation flow
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getPhoneNumber());
        String token = jwtUtil.generateToken(userDetails);

        logger.info("JWT token generated successfully for user: {}", request.getPhoneNumber());
        return ResponseEntity.ok(new JwtResponse(token));
    }

    private void authenticate(String username) throws ServiceException {
        try {
            logger.info("Attempting authentication for user: {}", username);
            //this internally calls loadsUserByUserName and check password from password encoder
            authenticationManager.authenticate(new PhoneNumberAuthenticationToken(username));
            logger.info("Authentication successful for user: {}", username);
        } catch (DisabledException e) {
            logger.error("User account is disabled: {}", username);
            throw new ServiceException("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            logger.error("Invalid credentials for user: {}", username);
            throw new ServiceException("INVALID_CREDENTIALS", e);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<SuccessResponse<UserDTO>> createUser(@Valid @RequestBody UserDTO userDTO) {
        logger.info("Registering new user with phone number: {}", userDTO.getPhoneNumber());

        UserDTO savedUser = userService.createUser(userDTO);

        logger.info("User registration successful. User ID: {}", savedUser.getId());
        return ResponseEntity.ok(new SuccessResponse<>("200", "User created successfully", List.of(savedUser)));
    }


}