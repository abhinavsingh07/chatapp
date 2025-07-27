package com.chatapp.synk.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class PhoneNumberAuthenticationToken extends AbstractAuthenticationToken {

    private String phoneNumberOrEmail;

    private String password;

    public PhoneNumberAuthenticationToken() {
        super(null);
    }

    public PhoneNumberAuthenticationToken(String phoneNumberOrEmail, String password) {
        super(null);
        this.phoneNumberOrEmail = phoneNumberOrEmail;
        this.password = password;
        setAuthenticated(false);
    }

    public PhoneNumberAuthenticationToken(UserDetails userDetails, Collection<? extends GrantedAuthority> authorities) {
        super(authorities); // this sets the internal authorities list for AbstractAuthenticationToken
        this.phoneNumberOrEmail = userDetails.getUsername(); // could also store separately if needed
        this.password = userDetails.getPassword();
    }


    @Override
    public Object getCredentials() {
        return password;
    }

    @Override
    public Object getPrincipal() {
        return phoneNumberOrEmail;
    }

    public void setPhoneNumberOrEmail(String phoneNumberOrEmail) {
        this.phoneNumberOrEmail = phoneNumberOrEmail;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
