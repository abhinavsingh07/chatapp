package com.chatapp.synk.security;

import com.chatapp.synk.controller.AuthController;
import com.chatapp.synk.entity.Users;
import com.chatapp.synk.repository.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    @Autowired
    private UsersRepository usersRepository;

    @Override
    @Cacheable(value = "userDetailsCache", key = "#phoneNumber", unless = "#result == null")
    public UserDetails loadUserByUsername(String phoneNumber) throws UsernameNotFoundException {
        Users user = usersRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UsernameNotFoundException("Users not found with phone: " + phoneNumber));
        logger.info("loadsUserByUsername called.. phonenumber::{}", user.getPhoneNumber());
        return new CustomUserDetails(
                user.getPhoneNumber(),  // treated as username
                "temporary-password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
