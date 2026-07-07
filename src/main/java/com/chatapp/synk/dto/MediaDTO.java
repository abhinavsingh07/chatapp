package com.chatapp.synk.dto;

import com.chatapp.synk.mediaUpload.enums.MediaType;
import com.chatapp.synk.mediaUpload.enums.MediaUploadStatus;
import com.chatapp.synk.mediaUpload.enums.MediaUsageType;

public class MediaDTO {

    private String id;
    private String ownerUserId;
    private String conversationId;
    private String s3Key;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private MediaType mediaType;
    private MediaUsageType usageType;
    private MediaUploadStatus status;
    private String createdAt;
    private String uploadedAt;
    private String clientUploadId;
    private String messageId;

    public MediaDTO() {
    }

    // Getters & Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(String ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(String uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public String getClientUploadId() {
        return clientUploadId;
    }

    public void setClientUploadId(String clientUploadId) {
        this.clientUploadId = clientUploadId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
