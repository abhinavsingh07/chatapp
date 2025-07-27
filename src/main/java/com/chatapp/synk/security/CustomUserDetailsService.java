package com.chatapp.synk.security;

import com.chatapp.synk.dto.UserDTO;
import com.chatapp.synk.exceptionHandler.ServiceException;
import com.chatapp.synk.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String phoneNumberOrEmail) throws UsernameNotFoundException {
        UserDTO userDTO = null;
        try {
            userDTO = userService.getUserByPhoneNumberOrEmail(phoneNumberOrEmail);
        } catch (ServiceException ex) {
            logger.error("Invalid identifier format: {}", phoneNumberOrEmail);
            throw new UsernameNotFoundException("Invalid phone number or email format");
        }

        if (userDTO == null) {
            logger.warn("No user found for identifier: {}", phoneNumberOrEmail);
            throw new UsernameNotFoundException("User not found with identifier: " + phoneNumberOrEmail);
        }

        logger.info("loadUserByUsername successful: identifier={}, phone={}, role={}", phoneNumberOrEmail, userDTO.getPhoneNumber(), "ROLE_USER");
        return new CustomUserDetails(userDTO.getPhoneNumber(),  // treated as username
                userDTO.getPassword(), Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }

}
