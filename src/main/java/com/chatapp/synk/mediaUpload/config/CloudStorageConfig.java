package com.chatapp.synk.mediaUpload.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.chatapp.synk.config.AppProperties;
import com.chatapp.synk.mediaUpload.service.CloudStorageService;
import com.chatapp.synk.mediaUpload.service.impl.S3StorageService;

import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class CloudStorageConfig {

    @Bean
    @ConditionalOnProperty(name = "cloud.provider", havingValue = "aws", matchIfMissing = true)
    public CloudStorageService awsStorageService(S3Client s3Client, AppProperties appProperties) {
        return new S3StorageService(s3Client, appProperties);
    }

    // @Bean
    // @ConditionalOnProperty(name = "cloud.provider", havingValue = "azure")
    // public CloudStorageService azureStorageService(BlobClient blobClient,
    // AppProperties appProperties) {
    // // return new AzureStorageService(blobClient);
    // }

    // @Bean
    // @ConditionalOnProperty(name = "cloud.provider", havingValue = "gcp")
    // public CloudStorageService gcpStorageService(Storage storage, AppProperties
    // appProperties) {
    // //return new GcpStorageService(storage, appProperties.getGcpBucketName());
    // }
}