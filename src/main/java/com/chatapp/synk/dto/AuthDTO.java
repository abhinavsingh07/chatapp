package com.chatapp.synk.dto;

import jakarta.validation.constraints.NotBlank;

public class AuthDTO {
    @NotBlank(message = "Phone number or Email is required")
    private String phoneNumberOrEmail;

    @NotBlank(message = "Password is required")
    private String password;

    public AuthDTO() {
    }

    public String getPhoneNumberOrEmail() {
        return phoneNumberOrEmail;
    }

    public void setPhoneNumberOrEmail(String phoneNumberOrEmail) {
        this.phoneNumberOrEmail = phoneNumberOrEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
