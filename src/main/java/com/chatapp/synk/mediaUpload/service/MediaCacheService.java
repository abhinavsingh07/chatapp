package com.chatapp.synk.mediaUpload.service;

/**
 * Manages cache eviction for media-related operations.
 * Separated from {@link MediaUploadService} to keep cache concerns
 * distinct from upload lifecycle (SRP).
 */
public interface MediaCacheService {

    /**
     * Evict profile-picture-related caches for a user.
     * Called after a profile picture upload completes so the next read
     * fetches fresh data from the database.
     *
     * @param userId the user whose profile picture was updated
     */
    void evictProfilePictureCaches(Long userId);
}
