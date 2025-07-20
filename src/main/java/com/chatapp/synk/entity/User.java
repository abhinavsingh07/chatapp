package com.chatapp.synk.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", schema = "chatapp")
public class User {

    @Id
    @Column(name = "id", nullable = false,length = 100)
    private String id;

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "profile_picture_url", length = 255)
    private String profilePictureUrl;

    @Column(name = "about", columnDefinition = "TEXT")
    private String about;

    @Column(name = "created_at", nullable = false, updatable = false)
    //LocalDateTime is DATETIME data type in db
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    //LocalDateTime is DATETIME data type in db
    private LocalDateTime updatedAt;

    // Constructors
    public User() {
    }

    public User(String id, String phoneNumber, String name, String profilePictureUrl,
                String about, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.profilePictureUrl = profilePictureUrl;
        this.about = about;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Lifecycle hooks


}
