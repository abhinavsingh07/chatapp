package com.chatapp.synk.mediaUpload.dto;

public class MediaPreSignedUrlResponse {

    private String presignedDownloadUrl;
    private int downloadUrlExpiresInMinutes; // in minutes

    // Constructors
    public MediaPreSignedUrlResponse() {
    }

    public MediaPreSignedUrlResponse(String presignedDownloadUrl, int downloadUrlExpiresInMinutes) {
        this.presignedDownloadUrl = presignedDownloadUrl;
        this.downloadUrlExpiresInMinutes = downloadUrlExpiresInMinutes;
    }

    // Getters and Setters
    public String getPresignedDownloadUrl() {
        return presignedDownloadUrl;
    }

    public void setPresignedDownloadUrl(String presignedDownloadUrl) {
        this.presignedDownloadUrl = presignedDownloadUrl;
    }

    public int getDownloadUrlExpiresInMinutes() {
        return downloadUrlExpiresInMinutes;
    }

    public void setDownloadUrlExpiresInMinutes(int downloadUrlExpiresInMinutes) {
        this.downloadUrlExpiresInMinutes = downloadUrlExpiresInMinutes;
    }    

}
