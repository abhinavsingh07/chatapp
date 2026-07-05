package com.chatapp.synk.mediaUpload.dto;

public class MediaUploadCompleteResponse {

    private Long mediaId;
    private String status;
    private String message;

    // Constructors
    public MediaUploadCompleteResponse() {
    }

    public MediaUploadCompleteResponse(Long mediaId, String status, String message) {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
