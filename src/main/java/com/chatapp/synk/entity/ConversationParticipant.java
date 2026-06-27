package com.chatapp.synk.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversation_participants", schema = "chatapp")
public class ConversationParticipant {
    public static final String ALIAS_PARTICIPANT = "PART";
    @Id
    @GenericGenerator(name = "snowflake_gen", strategy = "com.chatapp.synk.config.snowflakeConfig.SnowflakeIdentifierGenerator")
    @GeneratedValue(generator = "snowflake_gen")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "identifier_id", length = 50)
    private String identifierId;

    @Column(name = "conversation_id", nullable = false,length = 50)
    private Long conversationId;

    @Column(name = "user_id", nullable = false,length = 50)
    private Long userId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public ConversationParticipant() {
    }

    public ConversationParticipant(String identifierId, Long conversationId, Long userId) {
        this.identifierId = identifierId;
        this.conversationId = conversationId;
        this.userId = userId;
    }


    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    // Getters and Setters


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

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
