package com.chatapp.synk.mediaUpload.dto;

public class MediaPreSignedUrlResponse {

    private String presignedDownloadUrl;
    private int downloadUrlExpiresInMinutes; // in minutes
    private String mediaId; // Optional: Include media details if needed
    private String mediaType;
    private String mediaName;


    // Constructors
    public MediaPreSignedUrlResponse() {
    }

    public MediaPreSignedUrlResponse(String presignedDownloadUrl, int downloadUrlExpiresInMinutes, String mediaId,
            String mediaType, String mediaName) {
        this.presignedDownloadUrl = presignedDownloadUrl;
        this.downloadUrlExpiresInMinutes = downloadUrlExpiresInMinutes;
        this.mediaId = mediaId;
        this.mediaType = mediaType;
        this.mediaName = mediaName;
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

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getMediaName() {
        return mediaName;
    }

    public void setMediaName(String mediaName) {
        this.mediaName = mediaName;
    }

 

}
