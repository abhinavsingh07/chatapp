package com.chatapp.synk.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "conversation_last_message", schema = "chatapp")
public class ConversationLastMessage {

    @Id
    @Column(name = "conversation_id", nullable = false, length = 50)//this is unique and PK our on duplicate key update query works.
    private Long conversationId;
    @Column(name = "message_id", nullable = false, length = 50)
    private Long messageId;
    @Column(name = "sender_id", nullable = false, length = 50)
    private Long senderId;
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;//Instant is UTC time client converts this time on their browser or mobile sdk and get its time according to it timezone
    @Column(name = "updated_at",nullable = false)
    //LocalDateTime is DATETIME data type in db
    private Instant updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    
}
