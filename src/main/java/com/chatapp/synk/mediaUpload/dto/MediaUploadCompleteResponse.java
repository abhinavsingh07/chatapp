package com.chatapp.synk.mediaUpload.dto;

import com.chatapp.synk.mediaUpload.enums.MediaUploadStatus;

public class MediaUploadCompleteResponse {

    private Long mediaId;
    private MediaUploadStatus status;
    private String message;

    // Constructors
    public MediaUploadCompleteResponse() {
    }

    public MediaUploadCompleteResponse(Long mediaId, MediaUploadStatus status, String message) {
        this.mediaId = mediaId;
        this.status = status;
        this.message = message;
    }

    // Getters and Setters
    public Long getMediaId() {
        return mediaId;
    }

    public void setMediaId(Long mediaId) {
        this.mediaId = mediaId;
    }

    public MediaUploadStatus getStatus() {
        return status;
    }

    public void setStatus(MediaUploadStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
