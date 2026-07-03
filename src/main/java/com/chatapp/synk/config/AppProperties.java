package com.chatapp.synk.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Centralized application configuration properties.
 * This class exposes all externalized configuration values from application.properties
 * via getters, making them easily accessible across the application.
 * 
 * Usage: Inject this component into any service/controller and access properties via getters.
 * Example: appProperties.getAwsS3BucketName()
 */
@Component
public class AppProperties {

    // ==================== AWS S3 Configuration ====================
    @Value("${aws.s3.bucket-name}")
    private String awsS3BucketName;

    @Value("${aws.s3.region}")
    private String awsS3Region;

    @Value("${aws.s3.access-key}")
    private String awsS3AccessKey;

    @Value("${aws.s3.secret-key}")
    private String awsS3SecretKey;

    @Value("${aws.s3.upload-url-expiry-minutes:10}")
    private int awsS3UploadUrlExpiryMinutes;

    @Value("${aws.s3.download-url-expiry-minutes:15}")
    private int awsS3DownloadUrlExpiryMinutes;

    // ==================== Cloud Provider Selection ====================
    @Value("${cloud.provider:aws}")
    private String cloudProvider;

    // ==================== Media Cleanup Configuration ====================
    @Value("${media.cleanup.pending-expiry-minutes:45}")
    private int mediaCleanupPendingExpiryMinutes;

    // ==================== Redis Configuration ====================
    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    // ==================== JWT Configuration ====================
    @Value("${jwt.secret}")
    private String jwtSecret;

    // ==================== AWS S3 Getters ====================

    /**
     * @return The S3 bucket name for media storage
     */
    public String getAwsS3BucketName() {
        return awsS3BucketName;
    }

    /**
     * @return The AWS region for S3 service
     */
    public String getAwsS3Region() {
        return awsS3Region;
    }

    /**
     * @return AWS access key ID for authentication
     */
    public String getAwsS3AccessKey() {
        return awsS3AccessKey;
    }

    /**
     * @return AWS secret access key for authentication
     */
    public String getAwsS3SecretKey() {
        return awsS3SecretKey;
    }

    /**
     * @return Pre-signed upload URL expiry time in minutes (default: 10)
     */
    public int getAwsS3UploadUrlExpiryMinutes() {
        return awsS3UploadUrlExpiryMinutes;
    }

    /**
     * @return Pre-signed download URL expiry time in minutes (default: 15)
     */
    public int getAwsS3DownloadUrlExpiryMinutes() {
        return awsS3DownloadUrlExpiryMinutes;
    }

    // ==================== Cloud Provider Getters ====================

    /**
     * @return The active cloud provider (aws, azure, gcp)
     */
    public String getCloudProvider() {
        return cloudProvider;
    }

    // ==================== Media Cleanup Getters ====================

    /**
     * @return Minutes after which pending uploads are considered expired for cleanup
     */
    public int getMediaCleanupPendingExpiryMinutes() {
        return mediaCleanupPendingExpiryMinutes;
    }

    // ==================== Redis Getters ====================

    /**
     * @return Redis server hostname
     */
    public String getRedisHost() {
        return redisHost;
    }

    /**
     * @return Redis server port
     */
    public int getRedisPort() {
        return redisPort;
    }

    // ==================== JWT Getters ====================

    /**
     * @return JWT secret key for token signing and validation
     */
    public String getJwtSecret() {
        return jwtSecret;
    }
}
