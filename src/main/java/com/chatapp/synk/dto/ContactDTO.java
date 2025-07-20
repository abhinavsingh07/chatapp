package com.chatapp.synk.dto;

import jakarta.validation.constraints.NotBlank;

public class ContactDTO {

    private String id;
    @NotBlank(message = "UserId is required")
    private String userId;
    @NotBlank(message = "Contact UserId is required")
    private String contactUserId;

    public ContactDTO(){
    }

    public ContactDTO(String id, String userId, String contactUserId) {
        this.id = id;
        this.userId = userId;
        this.contactUserId = contactUserId;
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
}
