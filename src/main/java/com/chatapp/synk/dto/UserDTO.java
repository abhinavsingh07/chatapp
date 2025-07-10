package com.chatapp.synk.dto;


import jakarta.validation.constraints.NotBlank;

public class UserDTO {
    private String id;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "Name is required")
    private String name;
    private String profilePictureUrl;
    private String about;

    public UserDTO() {
    }

    public UserDTO(String id, String phoneNumber, String name, String profilePictureUrl, String about) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.profilePictureUrl = profilePictureUrl;
        this.about = about;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }
}
