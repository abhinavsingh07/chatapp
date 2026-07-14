package com.chatapp.synk.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import com.chatapp.synk.mediaUpload.enums.MediaType;
import com.chatapp.synk.mediaUpload.enums.MediaUploadStatus;
import com.chatapp.synk.mediaUpload.enums.MediaUsageType;

import java.time.Instant;

@Entity
@Table(name = "media", schema = "chatapp")
public class Media {

    public static final String ALIAS_MEDIA = "MED";

    @Id
    @GenericGenerator(name = "snowflake_gen", strategy = "com.chatapp.synk.config.snowflakeConfig.SnowflakeIdentifierGenerator")
    @GeneratedValue(generator = "snowflake_gen")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;

    @Column(name = "conversation_id")
    private Long conversationId;

    @Column(name = "s3_key", length = 500, nullable = false)
    private String s3Key;

    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "media_type")
    @Enumerated(EnumType.STRING)
    private MediaType mediaType;

    @Column(name = "usage_type")
    @Enumerated(EnumType.STRING)
    private MediaUsageType usageType;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private MediaUploadStatus status = MediaUploadStatus.UPLOAD_PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt; // Instant is UTC time client converts this time on their browser or mobile sdk
                               // and get its time according to it timezone

    @Column(name = "uploaded_at")
    private Instant uploadedAt;

    @Column(name = "client_upload_id", length = 100, unique = true)
    private String clientUploadId;

    @Column(name = "message_id")
    private Long messageId;

    // Constructors
    public Media() {
    }

    public Media(Long ownerUserId, String s3Key, String fileName, String contentType, Long fileSize,
            MediaType mediaType, MediaUsageType usageType) {
        this.ownerUserId = ownerUserId;
        this.s3Key = s3Key;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.mediaType = mediaType;
        this.usageType = usageType;
    }

    // Lifecycle hooks
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now(); // Always UTC
        if (status == null) {
            status = MediaUploadStatus.UPLOAD_PENDING; // Default status
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(Long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public MediaUsageType getUsageType() {
        return usageType;
    }

    public void setUsageType(MediaUsageType usageType) {
        this.usageType = usageType;
    }

    public MediaUploadStatus getStatus() {
        return status;
    }

    public void setStatus(MediaUploadStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public String getClientUploadId() {
        return clientUploadId;
    }

    public void setClientUploadId(String clientUploadId) {
        this.clientUploadId = clientUploadId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }
}
