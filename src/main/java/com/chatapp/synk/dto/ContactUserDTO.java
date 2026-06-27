package com.chatapp.synk.dto;

import com.chatapp.synk.enums.ContactStatus;
import com.chatapp.synk.enums.EmailStatus;
import com.chatapp.synk.enums.UserStatus;

public class ContactUserDTO {
    private Long contactId;
    private Long userId;
    private ContactStatus contactStatus;
    private EmailStatus emailStatus;
    private Long contactUserId;
    private String contactEmail;
    private String identifierId;

    // user table fields
    private String name;
    private String phoneNumber;
    private String email;
    private String profilePictureUrl;
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
            String profilePictureUrl,
            UserStatus status) {

        this.contactId = contactId;
        this.contactStatus = contactStatus;
        this.emailStatus = emailStatus;
        this.contactUserId = contactUserId;
        this.contactEmail = contactEmail;
        this.userId = userId;
        this.identifierId = identifierId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.profilePictureUrl = profilePictureUrl;
        this.status = status;

    }

    public ContactUserDTO() {
    }

    public Long getContactId() {
        return contactId;
    }

    public void setContactId(Long contactId) {
        this.contactId = contactId;
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

    public Long getContactUserId() {
        return contactUserId;
    }

    public void setContactUserId(Long contactUserId) {
        this.contactUserId = contactUserId;
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

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getIdentifierId() {
        return identifierId;
    }

    public void setIdentifierId(String identifierId) {
        this.identifierId = identifierId;
    }

}
