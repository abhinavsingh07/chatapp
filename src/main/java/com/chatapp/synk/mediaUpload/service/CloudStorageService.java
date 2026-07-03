package com.chatapp.synk.mediaUpload.service;

/**
 * Abstraction for cloud storage operations.
 * Allows different cloud provider implementations (AWS S3, Azure Blob, Google Cloud Storage, etc.)
 * without changing the application logic.
 * 
 * Implementations: S3StorageService, AzureStorageService, GcpStorageService, etc.
 */
public interface CloudStorageService {

    /**
     * Generate a pre-signed URL for uploading to cloud storage.
     *
     * @param key                  Object key/path
     * @param uploadExpiryMinutes   URL expiry in minutes
     * @return Pre-signed upload URL
     */
    String generatePreSignedPutUrl(String key, int uploadExpiryMinutes);

    /**
     * Generate a pre-signed URL for downloading from cloud storage.
     *
     * @param key                    Object key/path
     * @param downloadExpiryMinutes  URL expiry in minutes
     * @return Pre-signed download URL
     */
    String generatePreSignedGetUrl(String key, int downloadExpiryMinutes);

    /**
     * Verify if an object exists in cloud storage.
     *
     * @param key Object key/path
     * @return true if exists, false otherwise
     */
    boolean verifyObjectExists(String key);

    /**
     * Delete an object from cloud storage.
     *
     * @param key Object key/path
     * @return true if deleted, false if not found
     */
    boolean deleteObject(String key);
}
