package com.chatapp.synk.mediaUpload.service;

import com.chatapp.synk.config.AppProperties;
import com.chatapp.synk.entity.Media;
import com.chatapp.synk.mediaUpload.enums.MediaUploadStatus;
import com.chatapp.synk.mediaUpload.repository.MediaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Scheduled cleanup service for expired/abandoned media uploads.
 * Removes UPLOAD_PENDING media records that have not been completed within the
 * configured time window.
 * Also attempts to clean up orphaned cloud storage objects.
 */
@Component
public class MediaCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(MediaCleanupService.class);

    private final MediaRepository mediaRepository;
    private final CloudStorageService awsStorageService;
    private final AppProperties appProperties;

    public MediaCleanupService(MediaRepository mediaRepository, CloudStorageService awsStorageService,
            AppProperties appProperties) {
        this.mediaRepository = mediaRepository;
        this.awsStorageService = awsStorageService;
        this.appProperties = appProperties;
    }

    /**
     * Scheduled cleanup job: runs every hour to clean up abandoned uploads.
     * - Finds all UPLOAD_PENDING media created more than 45 minutes ago
     * - Attempts to delete S3 objects
     * - Deletes or marks as FAILED in database
     * 
     * Configured to run every 1 hour (3600000 ms).
     */
    @Scheduled(fixedRate = 3600000) // Run every 1 hour
    public void cleanupExpiredPendingMedia() {
        int pendingExpiryMinutes = appProperties.getMediaCleanupPendingExpiryMinutes();
        logger.info("Starting cleanup of expired UPLOAD_PENDING media (older than {} minutes)", pendingExpiryMinutes);

        try {
            // Calculate cutoff timestamp: now minus pendingExpiryMinutes
            Instant cutoffTime = Instant.now().minusSeconds((long) pendingExpiryMinutes * 60);
            logger.debug("Cutoff time for cleanup: {}", cutoffTime);

            // Find all UPLOAD_PENDING media older than cutoff
            List<Media> expiredMedia = mediaRepository
                    .findByStatusAndCreatedAtBefore(MediaUploadStatus.UPLOAD_PENDING, cutoffTime);

            if (expiredMedia.isEmpty()) {
                logger.info("No expired UPLOAD_PENDING media found");
                return;
            }

            logger.info("Found {} expired UPLOAD_PENDING media records", expiredMedia.size());

            // Process each expired media — S3 deletes are individual (external API),
            // DB deletes are batched into a single query
            int successCount = 0;
            int failureCount = 0;
            List<Media> recordsToDelete = new ArrayList<>();

            for (Media media : expiredMedia) {
                try {
                    // Attempt to delete cloud storage object
                    boolean deleted = awsStorageService.deleteObject(media.getS3Key());
                    if (deleted) {
                        logger.debug("Deleted cloud storage object: {}", media.getS3Key());
                    } else {
                        logger.warn("Cloud storage object not found (may already be deleted): {}", media.getS3Key());
                    }

                    // Collect for batch DB delete
                    recordsToDelete.add(media);
                    logger.info("Cleaned up media ID: {} (userId: {}, s3Key: {})",
                            media.getId(), media.getOwnerUserId(), media.getS3Key());
                    successCount++;

                } catch (Exception e) {
                    logger.error("Error cleaning up media ID: {} (s3Key: {}). Will retry in next cleanup cycle.",
                            media.getId(), media.getS3Key(), e);
                    failureCount++;
                    // Continue processing other records even if one fails
                }
            }

            // Batch DB delete — single query instead of N individual DELETE calls
            if (!recordsToDelete.isEmpty()) {
                mediaRepository.deleteAllInBatch(recordsToDelete);
            }

            logger.info("Cleanup completed: {} successful, {} failed out of {} media records",
                    successCount, failureCount, expiredMedia.size());

        } catch (Exception e) {
            logger.error("Fatal error during media cleanup. Will retry in next scheduled run.", e);
            // Don't rethrow - allow scheduler to continue running
        }
    }
}
