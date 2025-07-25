package com.chatapp.synk.security;

import com.chatapp.synk.entity.User;
import com.chatapp.synk.repository.UserRepository;
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
    private UserRepository userRepository;

    @Override
    @Cacheable(value = "userDetailsCache", key = "#phoneNumber", unless = "#result == null")
    public UserDetails loadUserByUsername(String phoneNumber) throws UsernameNotFoundException {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with phone: " + phoneNumber));
        logger.info("loadsUserByUsername called.. phonenumber::{} , passord::{}", user.getPhoneNumber(),user.getPassword());
        return new CustomUserDetails(
                user.getPhoneNumber(),  // treated as username
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
