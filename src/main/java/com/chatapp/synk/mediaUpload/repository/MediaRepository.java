package com.chatapp.synk.mediaUpload.repository;

import com.chatapp.synk.entity.Media;
import com.chatapp.synk.mediaUpload.enums.MediaUploadStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {

    /**
     * Find media by ID and owner user ID (ownership verification).
     *
     * @param id         Media ID
     * @param ownerUserId User ID (owner)
     * @return Optional containing Media if found and owned by user, empty otherwise
     */
    Optional<Media> findByIdAndOwnerUserId(Long id, Long ownerUserId);

    /**
     * Find media by clientUploadId for idempotency checking.
     *
     * @param clientUploadId Unique client upload ID provided by frontend
     * @return Optional containing Media if found
     */
    Optional<Media> findByClientUploadId(String clientUploadId);

    /**
     * Find all UPLOAD_PENDING media created before a specific timestamp.
     * Used for cleanup of expired uploads.
     *
     * @param status   Media status (UPLOAD_PENDING)
     * @param timestamp Cutoff timestamp (e.g., now - 45 minutes)
     * @return List of expired media
     */
    List<Media> findByStatusAndCreatedAtBefore(MediaUploadStatus status, Instant timestamp);

    /**
     * Update media status by ID and owner user ID (transactional operation).
     * Ensures only the owner can update their media.
     *
     * @param id         Media ID
     * @param ownerUserId User ID (owner)
     * @param status     New status
     * @param uploadedAt Timestamp of upload completion
     * @return Number of records updated
     */
    @Modifying
    @Transactional
    @Query("UPDATE Media m SET m.status = :status, m.uploadedAt = :uploadedAt " +
            "WHERE m.id = :id AND m.ownerUserId = :ownerUserId")
    int updateStatusAndUploadedAtByIdAndOwnerUserId(
            @Param("id") Long id,
            @Param("ownerUserId") Long ownerUserId,
            @Param("status") MediaUploadStatus status,
            @Param("uploadedAt") Instant uploadedAt);

    /**
     * Delete all media matching status and created before timestamp.
     * Used for bulk cleanup of expired uploads.
     *
     * @param status   Media status (UPLOAD_PENDING)
     * @param timestamp Cutoff timestamp
     * @return Number of records deleted
     */
    @Modifying
    @Transactional
    int deleteByStatusAndCreatedAtBefore(MediaUploadStatus status, Instant timestamp);

    /**
     * Find media by ID and conversation ID (for viewing media in conversations).
     * Allows conversation participants to access media shared in their conversation.
     *
     * @param id Media ID
     * @param conversationId Conversation ID
     * @return Optional containing Media if found and associated with conversation
     */
    Optional<Media> findByIdAndConversationId(Long id, Long conversationId);

    /**
     * Update message ID for media records by media ID and owner user ID.
     *
     * @param id         Media ID
     * @param ownerUserId User ID (owner)
     * @param messageId   Message ID to associate with media
     * @return Number of records updated
     */
    @Modifying
    @Transactional
    @Query("UPDATE Media m SET m.messageId = :messageId " +
            "WHERE m.id = :id AND m.ownerUserId = :ownerUserId")
    int updateMessageIdByIdAndOwnerUserId(
            @Param("id") Long id,
            @Param("ownerUserId") Long ownerUserId,
            @Param("messageId") Long messageId);
}
