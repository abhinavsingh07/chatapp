package com.chatapp.synk.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Custom AuthenticationProvider implementation that validates users based solely on their phone number.
 * <p>
 * This provider should be explicitly configured in SecurityConfig and registered with the AuthenticationManager
 * to enable.
 * DaoAuthenticationProvider also written in this way
 * This class need to be configure in securityConfig and pass to authentication manager.
 * <p>
 * It works in tandem with a custom AuthenticationToken (PhoneNumberAuthenticationToken) and a UserDetailsService
 * implementation that loads users via phone numbers rather than traditional usernames.
 * <p>
 * Usage Scenario:
 * - Ideal for applications where phone number acts as the primary user identifier.
 * - Common in stateless authentication flows, especially when used alongside JWTs and custom filters.
 */
@Component
public class PhoneNumberAuthenticationProvider implements AuthenticationProvider {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private UserDetailsService userDetailsService;//passing from security config inside authenticationProvider() method

    private PasswordEncoder passwordEncoder;//passing from security config inside authenticationProvider() method

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String phoneNumber = authentication.getPrincipal().toString();
        String password = authentication.getCredentials().toString();
        UserDetails userDetails = userDetailsService.loadUserByUsername(phoneNumber);
        logger.debug("This PhoneNumberAuthenticationProvider authenticate method calls by authentication manager..");
        if (userDetails.getUsername().equals(phoneNumber)) {
            if (!passwordEncoder.matches(password, userDetails.getPassword())) {
                throw new BadCredentialsException("Invalid password");
            }
            PhoneNumberAuthenticationToken authenticatedToken = new PhoneNumberAuthenticationToken();
            authenticatedToken.setDetails(userDetails);
            authenticatedToken.setAuthenticated(true);
            return authenticatedToken;
        }
        throw new BadCredentialsException("Phone number mismatch");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        logger.debug("supports() called with: {}", authentication.getName());
        //important step to make our authentication provider to work.
        return PhoneNumberAuthenticationToken.class.isAssignableFrom(authentication);
    }

    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
}
