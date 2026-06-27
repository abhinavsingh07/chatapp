package com.chatapp.synk.entity;

import com.chatapp.synk.enums.RoleName;
import com.chatapp.synk.enums.UserStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "users", schema = "chatapp", uniqueConstraints = @UniqueConstraint(name = "uq_phone_email", columnNames = {
        "phone_number", "email" }))
public class User {
    public static final String ALIAS_USER = "USER";
    @Id
    @GenericGenerator(name = "snowflake_gen", strategy = "com.chatapp.synk.config.snowflakeConfig.SnowflakeIdentifierGenerator")
    @GeneratedValue(generator = "snowflake_gen")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "password", length = 200)
    private String password;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "profile_picture_url", length = 255)
    private String profilePictureUrl;

    @Column(name = "about", columnDefinition = "TEXT")
    private String about;

    @Column(name = "created_at", nullable = false, updatable = false)
    // LocalDateTime is DATETIME data type in db
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    // LocalDateTime is DATETIME data type in db
    private LocalDateTime updatedAt;
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Column(name = "user_role")
    @Enumerated(EnumType.STRING)
    private RoleName userRole;

    @Column(name = "user_last_seen")
    private String userlastSeen;

    @Column(name = "identifier_id", length = 50)
    private String identifierId;

    // Constructors
    public User() {
    }

    public User(Long id, String phoneNumber, String email,
            String password, String name, String profilePictureUrl,
            String about, LocalDateTime createdAt, LocalDateTime updatedAt) {

        this.id = id;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.password = password;
        this.name = name;
        this.profilePictureUrl = profilePictureUrl;
        this.about = about;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Lifecycle hooks
    @PrePersist
    protected void onCreate() {

        createdAt = updatedAt = LocalDateTime.now();
        if (status == null) {
            status = UserStatus.ACTIVE; // Default status
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserlastSeen() {
        return userlastSeen;
    }

    public void setUserlastSeen(String userlastSeen) {
        this.userlastSeen = userlastSeen;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public RoleName getUserRole() {
        return userRole;
    }

    public void setUserRole(RoleName userRole) {
        this.userRole = userRole;
    }

    public String getIdentifierId() {
        return identifierId;
    }

    public void setIdentifierId(String identifierId) {
        this.identifierId = identifierId;
    }


}
