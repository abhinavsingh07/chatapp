package com.chatapp.synk.security;

import java.io.Serializable;
import java.util.Date;

public class JwtResponse implements Serializable {
    private static final long serialVersionUID = -8091879091924046844L;
    private final String jwtToken;

    private Date expiry;

    public JwtResponse(String jwtToken) {
        this.jwtToken = jwtToken;
        this.expiry = new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10);
    }

    public String getToken() {
        return this.jwtToken;
    }

    public Date getExpiry() {
        return expiry;
    }
}
