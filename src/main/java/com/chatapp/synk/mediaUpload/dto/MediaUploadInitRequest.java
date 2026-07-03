package com.chatapp.synk.mediaUpload.dto;

import com.chatapp.synk.mediaUpload.enums.MediaType;
import com.chatapp.synk.mediaUpload.enums.MediaUsageType;

import jakarta.validation.constraints.*;

public class MediaUploadInitRequest {

    @NotNull(message = "mediaType is required")
    private MediaType mediaType;

    @NotNull(message = "usageType is required")
    private MediaUsageType usageType;

    @NotBlank(message = "contentType is required")
    private String contentType;

    @NotNull(message = "fileSize is required")
    @Min(value = 1, message = "fileSize must be greater than 0")
    private Long fileSize;

    @NotBlank(message = "fileName is required")
    private String fileName;

    private Long conversationId;

    private String clientUploadId; // Optional idempotency key for retry handling

    // Constructors
    public MediaUploadInitRequest() {
    }

    public MediaUploadInitRequest(MediaType mediaType, MediaUsageType usageType, String contentType,
            Long fileSize, String fileName) {
        this.mediaType = mediaType;
        this.usageType = usageType;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.fileName = fileName;
    }

    // Getters and Setters
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public String getClientUploadId() {
        return clientUploadId;
    }

    public void setClientUploadId(String clientUploadId) {
        this.clientUploadId = clientUploadId;
    }
}
