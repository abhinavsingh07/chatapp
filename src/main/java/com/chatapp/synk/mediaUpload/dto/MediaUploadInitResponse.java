package com.chatapp.synk.mediaUpload.dto;

public class MediaUploadInitResponse {

    private Long mediaId;
    private String presignedUploadUrl;
    private int uploadUrlExpiresIn; // in seconds

    // Constructors
    public MediaUploadInitResponse() {
    }

    public MediaUploadInitResponse(Long mediaId, String presignedUploadUrl, int uploadUrlExpiresIn) {
        this.mediaId = mediaId;
        this.presignedUploadUrl = presignedUploadUrl;
        this.uploadUrlExpiresIn = uploadUrlExpiresIn;
    }

    // Getters and Setters
    public Long getMediaId() {
        return mediaId;
    }

    public void setMediaId(Long mediaId) {
        this.mediaId = mediaId;
    }

    public String getPresignedUploadUrl() {
        return presignedUploadUrl;
    }

    public void setPresignedUploadUrl(String presignedUploadUrl) {
        this.presignedUploadUrl = presignedUploadUrl;
    }

    public int getUploadUrlExpiresIn() {
        return uploadUrlExpiresIn;
    }

    public void setUploadUrlExpiresIn(int uploadUrlExpiresIn) {
        this.uploadUrlExpiresIn = uploadUrlExpiresIn;
    }
}
