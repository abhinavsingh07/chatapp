package com.chatapp.synk.dto;

import jakarta.validation.constraints.NotBlank;

public class AuthDTO {
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
    @NotBlank(message = "Password is required")
    private String password;

    public AuthDTO() {
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
