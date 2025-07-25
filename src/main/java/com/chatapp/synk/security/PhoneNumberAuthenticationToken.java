package com.chatapp.synk.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class PhoneNumberAuthenticationToken extends AbstractAuthenticationToken {

    private String phoneNumber;

    private String password;

    public PhoneNumberAuthenticationToken() {
        super(null);
    }

    public PhoneNumberAuthenticationToken(String phoneNumber, String password) {
        super(null);
        this.phoneNumber = phoneNumber;
        this.password = password;
        setAuthenticated(false);
    }

    @Override
    public Object getCredentials() {
        return password;
    }

    @Override
    public Object getPrincipal() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
