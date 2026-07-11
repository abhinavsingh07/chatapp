package com.chatapp.synk.dto;

import com.chatapp.synk.enums.ContactStatus;
import com.chatapp.synk.enums.EmailStatus;
import com.chatapp.synk.enums.UserStatus;

public class ContactUserDTO {
    private String contactId;
    private String userId;
    private ContactStatus contactStatus;
    private EmailStatus emailStatus;
    private String contactUserId;
    private String contactEmail;
    private String identifierId;

    // user table fields
    private String name;
    private String phoneNumber;
    private String email;
    // private String profilePictureUrl;
    private String mediaId;
    private UserStatus status;

    // using JPQL query and normal object mapping in repository layer to set these
    // fields
    public ContactUserDTO(Long contactId,
            ContactStatus contactStatus,
            EmailStatus emailStatus,
            Long contactUserId,
            String contactEmail,
            Long userId,
            String identifierId,
            String name,
            String phoneNumber,
            String email,
            String mediaId,
            UserStatus status) {

        this.contactId = String.valueOf(contactId);
        this.contactStatus = contactStatus;
        this.emailStatus = emailStatus;
        this.contactUserId = String.valueOf(contactUserId);
        this.contactEmail = contactEmail;
        this.userId = String.valueOf(userId);
        this.identifierId = identifierId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.mediaId = mediaId;
        this.status = status;

    }

    public ContactUserDTO() {
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
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

    public ContactStatus getContactStatus() {
        return contactStatus;
    }

    public void setContactStatus(ContactStatus contactStatus) {
        this.contactStatus = contactStatus;
    }

    public EmailStatus getEmailStatus() {
        return emailStatus;
    }

    public void setEmailStatus(EmailStatus emailStatus) {
        this.emailStatus = emailStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getIdentifierId() {
        return identifierId;
    }

    public void setIdentifierId(String identifierId) {
        this.identifierId = identifierId;
    }

}
