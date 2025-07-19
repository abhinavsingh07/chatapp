package com.chatapp.synk.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class PhoneNumberAuthenticationToken extends AbstractAuthenticationToken {

    private String phoneNumber;

    public PhoneNumberAuthenticationToken() {
        super(null);
    }

    public PhoneNumberAuthenticationToken(String phoneNumber) {
        super(null);
        this.phoneNumber = phoneNumber;
        setAuthenticated(false);
    }

    @Override
    public Object getCredentials() {
        return phoneNumber;
    }

    @Override
    public Object getPrincipal() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
