package com.chatapp.synk.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class ContactsDTO {

    private String id;
    @NotBlank(message = "UserId is required")
    private String userId;
    @NotBlank(message = "Contact UserId is required")
    private String contactUserId;
    private LocalDateTime createdAt;

    public ContactsDTO(){
    }

    public ContactsDTO(String id, String userId, String contactUserId, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.contactUserId = contactUserId;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContactUserId() {
        return contactUserId;
    }

    public void setContactUserId(String contactUserId) {
        this.contactUserId = contactUserId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
