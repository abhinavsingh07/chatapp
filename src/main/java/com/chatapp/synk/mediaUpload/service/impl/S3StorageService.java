package com.chatapp.synk.mediaUpload.service.impl;

import com.chatapp.synk.config.AppProperties;
import com.chatapp.synk.exceptionHandler.ServiceException;
import com.chatapp.synk.mediaUpload.service.CloudStorageService;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;

/**
 * AWS S3 implementation of CloudStorageService.
 * Handles all S3-specific operations for object storage.
 * 
 * Optimizations:
 * - S3Presigner is created once and reused for all presigned URL operations
 * (thread-safe)
 * - Region is injected from AppProperties to avoid expensive metadata lookups
 * - Resources are cleaned up on application shutdown via @PreDestroy
 */
@Service
public class S3StorageService implements CloudStorageService {

    private static final Logger logger = LoggerFactory.getLogger(S3StorageService.class);

    private final S3Client s3Client;
    private final String bucketName;
    private final S3Presigner s3Presigner;
    private final String region;

    public S3StorageService(S3Client s3Client, String bucketName, AppProperties appProperties) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.region = appProperties.getAwsS3Region();
        // Create S3Presigner once and reuse for all operations (thread-safe)
        this.s3Presigner = S3Presigner.builder()
                .region(Region.of(this.region))
                .build();
        logger.info("S3StorageService initialized with bucket: {}, region: {}", bucketName, region);
    }

    /**
     * Generates a pre-signed PUT URL for secure direct upload to S3.
     */
    @Override
    public String generatePreSignedPutUrl(String key, int uploadExpiryMinutes) {
        try {
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("S3 key must not be blank");
            }

            if (uploadExpiryMinutes <= 0 || uploadExpiryMinutes > 60) {
                throw new IllegalArgumentException("Upload expiry must be between 1 and 60 minutes");
            }

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(uploadExpiryMinutes))
                    .putObjectRequest(putObjectRequest)
                    .build();

            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
            String presignedUrl = presignedRequest.url().toString();

            logger.debug("Generated pre-signed PUT URL for key: {}", key);
            return presignedUrl;

        } catch (Exception e) {
            logger.error("Failed to generate pre-signed PUT URL for key: {}", key, e);
            throw new ServiceException("Failed to generate upload URL", e);
        }
    }

    /**
     * Generates a pre-signed GET URL for secure download from S3.
     */
    @Override
    public String generatePreSignedGetUrl(String key, int downloadExpiryMinutes) {
        try {

            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("S3 key must not be blank");
            }

            if (downloadExpiryMinutes <= 0 || downloadExpiryMinutes > 60) {
                throw new IllegalArgumentException("Download expiry must be between 1 and 60 minutes");
            }

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(downloadExpiryMinutes))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            String presignedUrl = presignedRequest.url().toString();

            logger.debug("Generated pre-signed GET URL for key: {}", key);
            return presignedUrl;

        } catch (Exception e) {
            logger.error("Failed to generate pre-signed GET URL for key: {}", key, e);
            throw new ServiceException("Failed to generate download URL", e);
        }
    }

    /**
     * Checks if an object exists in S3 without downloading it.
     */
    @Override
    public boolean verifyObjectExists(String key) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.headObject(headObjectRequest);
            logger.debug("Verified S3 object exists: {}", key);
            return true;

        } catch (NoSuchKeyException e) {
            logger.warn("S3 object not found: {}", key);
            return false;
        } catch (Exception e) {
            logger.error("Error verifying S3 object: {}", key, e);
            throw new ServiceException("Failed to verify S3 object", e);
        }
    }

    /**
     * Deletes an object from S3 storage.
     */
    @Override
    public boolean deleteObject(String key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            logger.debug("Deleted S3 object: {}", key);
            return true;

        } catch (NoSuchKeyException e) {
            logger.warn("S3 object not found (already deleted?): {}", key);
            return false;
        } catch (Exception e) {
            logger.error("Failed to delete S3 object: {}", key, e);
            throw new ServiceException("Failed to delete S3 object", e);
        }
    }

    /**
     * Cleanup lifecycle method called on application shutdown.
     * Closes the reusable S3Presigner to release AWS resources.
     */
    @PreDestroy
    public void close() {
        try {
            if (s3Presigner != null) {
                s3Presigner.close();
                logger.info("S3Presigner closed successfully");
            }
        } catch (Exception e) {
            logger.error("Error closing S3Presigner", e);
        }
    }
}
