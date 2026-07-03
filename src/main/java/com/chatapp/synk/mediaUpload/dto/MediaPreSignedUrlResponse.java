package com.chatapp.synk.mediaUpload.dto;

public class MediaPreSignedUrlResponse {

    private String presignedDownloadUrl;
    private int urlExpiresIn; // in seconds

    // Constructors
    public MediaPreSignedUrlResponse() {
    }

    public MediaPreSignedUrlResponse(String presignedDownloadUrl, int urlExpiresIn) {
        this.presignedDownloadUrl = presignedDownloadUrl;
        this.urlExpiresIn = urlExpiresIn;
    }

    // Getters and Setters
    public String getPresignedDownloadUrl() {
        return presignedDownloadUrl;
    }

    public void setPresignedDownloadUrl(String presignedDownloadUrl) {
        this.presignedDownloadUrl = presignedDownloadUrl;
    }

    public int getUrlExpiresIn() {
        return urlExpiresIn;
    }

    public void setUrlExpiresIn(int urlExpiresIn) {
        this.urlExpiresIn = urlExpiresIn;
    }
}
