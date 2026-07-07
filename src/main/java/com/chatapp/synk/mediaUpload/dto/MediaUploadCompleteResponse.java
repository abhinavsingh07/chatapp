package com.chatapp.synk.mediaUpload.dto;

public class MediaUploadCompleteResponse {

    private String mediaId;
    private String status;
    private String message;

    // Constructors
    public MediaUploadCompleteResponse() {
    }

    public MediaUploadCompleteResponse(String mediaId, String status, String message) {
        this.mediaId = mediaId;
        this.status = status;
        this.message = message;
    }

    // Getters and Setters
    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
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
