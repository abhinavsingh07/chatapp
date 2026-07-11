package com.chatapp.synk.entity;

import com.chatapp.synk.enums.ContactStatus;
import com.chatapp.synk.enums.EmailStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contacts", schema = "chatapp", uniqueConstraints = @UniqueConstraint(name = "uq_user_contact", columnNames = {
        "user_id", "contact_user_id" }))
public class Contact {
    public static final String ALIAS_CONTACT = "CONT";
    @Id
    @GenericGenerator(name = "snowflake_gen", strategy = "com.chatapp.synk.config.snowflakeConfig.SnowflakeIdentifierGenerator")
    @GeneratedValue(generator = "snowflake_gen")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "identifier_id", length = 50)
    private String identifierId;

    @Column(name = "user_id", nullable = false, length = 50)
    private Long userId;

    @Column(name = "contact_user_id", length = 100)
    private Long contactUserId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "contact_status")
    @Enumerated(EnumType.STRING)
    private ContactStatus contactStatus;
    @Column(name = "email_status")
    @Enumerated(EnumType.STRING)
    private EmailStatus emailStatus;

    @Column(name = "email", length = 100)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    // User.id references CurrentEntity.contact_user_id
    @JoinColumn(name = "id", referencedColumnName = "contact_user_id", insertable = false, updatable = false)
    private User contactUser;

    @OneToMany(fetch = FetchType.LAZY)
    //Media.owner_user_id references CurrentEntity.contact_user_id
    @JoinColumn(name = "owner_user_id", referencedColumnName = "contact_user_id", insertable = false, updatable = false)
    private List<Media> contactUserMedia = new ArrayList<>();

    public Contact() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdentifierId() {
        return identifierId;
    }

    public void setIdentifierId(String identifierId) {
        this.identifierId = identifierId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getContactUserId() {
        return contactUserId;
    }

    public void setContactUserId(Long contactUserId) {
        this.contactUserId = contactUserId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User getContactUser() {
        return contactUser;
    }

    public void setContactUser(User contactUser) {
        this.contactUser = contactUser;
    }

    public List<Media> getContactUserMedia() {
        return contactUserMedia;
    }

    public void setContactUserMedia(List<Media> contactUserMedia) {
        this.contactUserMedia = contactUserMedia;
    }
}
