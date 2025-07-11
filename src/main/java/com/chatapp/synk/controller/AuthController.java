package com.chatapp.synk.controller;

import com.chatapp.synk.dto.AuthDTO;
import com.chatapp.synk.dto.UserDTO;
import com.chatapp.synk.exceptionHandler.ServiceException;
import com.chatapp.synk.response.SuccessResponse;
import com.chatapp.synk.service.CustomUserDetailsService;
import com.chatapp.synk.service.UserService;
import com.chatapp.synk.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;
    @Autowired
    private UserService userService;

    @PostMapping("/authenticate")
    public ResponseEntity<String> authenticate(@RequestBody AuthDTO request) throws ServiceException {
        authenticate(request.getPhoneNumber(),"temppassword");
        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getPhoneNumber());
        final String token = jwtUtil.generateToken(userDetails);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/register")
    public ResponseEntity<SuccessResponse<UserDTO>> createUser(@Valid @RequestBody UserDTO userDTO) {
        UserDTO savedUser = userService.createUser(userDTO);
        return ResponseEntity.ok(new SuccessResponse<>("200", "User created successfully", List.of(savedUser)));
    }

    private void authenticate(String username, String password) throws ServiceException {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new ServiceException("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new ServiceException("INVALID_CREDENTIALS", e);
        }
    }
}