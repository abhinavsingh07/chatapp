package com.chatapp.synk.mediaUpload.service.impl;

import com.chatapp.synk.mediaUpload.service.CloudStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
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
 */
@Service
public class S3StorageService implements CloudStorageService {

    private static final Logger logger = LoggerFactory.getLogger(S3StorageService.class);

    private final S3Client s3Client;
    private final String bucketName;

    public S3StorageService(S3Client s3Client, String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @Override
    public String generatePreSignedPutUrl(String key, int uploadExpiryMinutes) {
        try (S3Presigner presigner = S3Presigner.builder()
                .region(s3Client.serviceModel().serviceMetadata().region()).build()) {

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(uploadExpiryMinutes))
                    .putObjectRequest(putObjectRequest)
                    .build();

            PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
            String presignedUrl = presignedRequest.url().toString();

            logger.debug("Generated pre-signed PUT URL for key: {}", key);
            return presignedUrl;

        } catch (Exception e) {
            logger.error("Failed to generate pre-signed PUT URL for key: {}", key, e);
            throw new RuntimeException("Failed to generate upload URL", e);
        }
    }

    @Override
    public String generatePreSignedGetUrl(String key, int downloadExpiryMinutes) {
        try (S3Presigner presigner = S3Presigner.builder()
                .region(s3Client.serviceModel().serviceMetadata().region()).build()) {

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(downloadExpiryMinutes))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
            String presignedUrl = presignedRequest.url().toString();

            logger.debug("Generated pre-signed GET URL for key: {}", key);
            return presignedUrl;

        } catch (Exception e) {
            logger.error("Failed to generate pre-signed GET URL for key: {}", key, e);
            throw new RuntimeException("Failed to generate download URL", e);
        }
    }

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
            throw new RuntimeException("Failed to verify S3 object", e);
        }
    }

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
            throw new RuntimeException("Failed to delete S3 object", e);
        }
    }
}
