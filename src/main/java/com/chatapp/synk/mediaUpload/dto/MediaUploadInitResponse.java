package com.chatapp.synk.mediaUpload.dto;

public class MediaUploadInitResponse {

    private String mediaId;
    private String presignedUploadUrl;
    private int uploadUrlExpiresInMinutes; // in minutes

    // Constructors
    public MediaUploadInitResponse() {
    }

    public MediaUploadInitResponse(String mediaId, String presignedUploadUrl, int uploadUrlExpiresInMinutes) {
        this.mediaId = mediaId;
        this.presignedUploadUrl = presignedUploadUrl;
        this.uploadUrlExpiresInMinutes = uploadUrlExpiresInMinutes;
    }

    // Getters and Setters
    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getPresignedUploadUrl() {
        return presignedUploadUrl;
    }

    public void setPresignedUploadUrl(String presignedUploadUrl) {
        this.presignedUploadUrl = presignedUploadUrl;
    }

    public int getUploadUrlExpiresInMinutes() {
        return uploadUrlExpiresInMinutes;
    }

    public void setUploadUrlExpiresInMinutes(int uploadUrlExpiresInMinutes) {
        this.uploadUrlExpiresInMinutes = uploadUrlExpiresInMinutes;
    }
}
