package com.chatapp.synk.mediaUpload.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import com.chatapp.synk.config.AppProperties;

@Configuration
@ConditionalOnProperty(name = "cloud.provider", havingValue = "aws", matchIfMissing = true)
public class S3Config {

    private final AppProperties appProperties;

    public S3Config(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Bean
    // To create s3 client we need to provide region, access key and secret key.
    // These values are fetched from AppProperties which reads them
    // from application.properties file.
    // The S3Client bean is then available for injection
    // into other components that require S3 operations.
    public S3Client s3Client() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(
                appProperties.getAwsS3AccessKey(),
                appProperties.getAwsS3SecretKey());
        return S3Client.builder()
                .region(Region.of(appProperties.getAwsS3Region()))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }

}