package com.chatapp.synk.entity;

import com.chatapp.synk.enums.MessageStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "messages", schema = "chatapp")
public class Message {

    public static final String ALIAS_MESSAGE = "MESG";

    @Id
    @GenericGenerator(name = "snowflake_gen", strategy = "com.chatapp.synk.config.snowflakeConfig.SnowflakeIdentifierGenerator")
    @GeneratedValue(generator = "snowflake_gen")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "identifier_id", length = 50)
    private String identifierId;

    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "message_status")
    @Enumerated(EnumType.STRING)
    private MessageStatus messageStatus = MessageStatus.SENT;

    @Column(name = "sent_at", nullable = false, updatable = false)
    private Instant sentAt;//Instant is UTC time client converts this time on their browser or mobile sdk and get its time according to it timezone

    @Column(name = "client_message_id", length = 100, unique = true)
    private String clientMessageId;

    @OneToMany(fetch = FetchType.LAZY)
    //Media.message_id references CurrentEntity.id
    @JoinColumn(name = "message_id", referencedColumnName = "id", insertable = false, updatable = false)
    private List<Media> mediaList;

    @PrePersist
    protected void onCreate() {
        sentAt = Instant.now(); // Always UTC
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

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MessageStatus getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(MessageStatus messageStatus) {
        this.messageStatus = messageStatus;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }

    public String getClientMessageId() {
        return clientMessageId;
    }

    public void setClientMessageId(String clientMessageId) {
        this.clientMessageId = clientMessageId;
    }

    public List<Media> getMediaList() {
        return mediaList;
    }

    public void setMediaList(List<Media> mediaList) {
        this.mediaList = mediaList;
    }
}
