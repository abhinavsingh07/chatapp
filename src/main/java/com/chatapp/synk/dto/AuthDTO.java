package com.chatapp.synk.dto;

import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;

public class AuthDTO {
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    public AuthDTO() {
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
