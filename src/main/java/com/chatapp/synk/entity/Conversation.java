package com.chatapp.synk.entity;

import com.chatapp.synk.enums.ConversationType;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversations", schema = "chatapp")
public class Conversation {
    public static final String ALIAS_CONVERSATION = "CONV";
    @Id
    @GenericGenerator(name = "snowflake_gen", strategy = "com.chatapp.synk.config.snowflakeConfig.SnowflakeIdentifierGenerator")
    @GeneratedValue(generator = "snowflake_gen")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "identifier_id", length = 50)
    private String identifierId;

    @Column(name = "conversation_type", length = 10)
    private String conversationType = ConversationType.ONE_TO_ONE.toString();

    // Canonical key: smallerUserId + ":" + largerUserId — unique per user-pair
    @Column(name = "private_chat_key", length = 101, unique = true)
    private String privateChatKey;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Conversation(String identifierId, String conversationType, String privateChatKey) {
        this.identifierId = identifierId;
        this.conversationType = conversationType;
        this.privateChatKey = privateChatKey;
    }

    public Conversation() {
        // Default constructor
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

    public String getConversationType() {
        return conversationType;
    }

    public void setConversationType(String conversationType) {
        this.conversationType = conversationType;
    }

    public String getPrivateChatKey() {
        return privateChatKey;
    }

    public void setPrivateChatKey(String privateChatKey) {
        this.privateChatKey = privateChatKey;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
