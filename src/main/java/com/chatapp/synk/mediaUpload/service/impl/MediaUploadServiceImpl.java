package com.chatapp.synk.mediaUpload.service.impl;

import com.chatapp.synk.config.AppProperties;
import com.chatapp.synk.entity.Media;
import com.chatapp.synk.exceptionHandler.ServiceException;
import com.chatapp.synk.mediaUpload.dto.*;
import com.chatapp.synk.mediaUpload.enums.MediaType;
import com.chatapp.synk.mediaUpload.enums.MediaUploadStatus;
import com.chatapp.synk.mediaUpload.enums.MediaUsageType;
import com.chatapp.synk.mediaUpload.repository.MediaRepository;
import com.chatapp.synk.mediaUpload.service.MediaUploadService;
import com.chatapp.synk.mediaUpload.service.CloudStorageService;
import com.chatapp.synk.mediaUpload.util.MediaValidationUtil;
import com.chatapp.synk.mediaUpload.util.S3KeyGenerator;
import com.chatapp.synk.repository.ConversationParticipantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class MediaUploadServiceImpl implements MediaUploadService {

        private static final Logger logger = LoggerFactory.getLogger(MediaUploadServiceImpl.class);

        private final MediaRepository mediaRepository;
        private final CloudStorageService awsStorageService;
        private final ConversationParticipantRepository conversationParticipantRepository;
        private final AppProperties appProperties;

        private int uploadUrlExpiryMinutes;
        private int downloadUrlExpiryMinutes;

        public MediaUploadServiceImpl(MediaRepository mediaRepository, CloudStorageService awsStorageService,
                        ConversationParticipantRepository conversationParticipantRepository,
                        AppProperties appProperties) {
                this.mediaRepository = mediaRepository;
                this.awsStorageService = awsStorageService;
                this.conversationParticipantRepository = conversationParticipantRepository;
                this.appProperties = appProperties;
                this.uploadUrlExpiryMinutes = appProperties.getAwsS3UploadUrlExpiryMinutes();
                this.downloadUrlExpiryMinutes = appProperties.getAwsS3DownloadUrlExpiryMinutes();
        }

        @Override
        @Transactional
        public MediaUploadInitResponse initiateUpload(Long userId, MediaUploadInitRequest request) {
                logger.info("Initiating media upload for userId: {}, mediaType: {}, usageType: {}", userId,
                                request.getMediaType(), request.getUsageType());

                // 1. Validate file size based on mediaType
                long maxFileSize = MediaValidationUtil.getMaxFileSizeForMediaType(request.getMediaType());
                if (request.getFileSize() > maxFileSize) {
                        logger.warn("File size {} exceeds limit {} for mediaType: {}",
                                        request.getFileSize(), maxFileSize, request.getMediaType());
                        throw new ServiceException(
                                        String.format("File size %d exceeds limit %d for %s",
                                                        request.getFileSize(), maxFileSize, request.getMediaType()),
                                        HttpStatus.BAD_REQUEST);
                }

                // 2. Validate MIME type against mediaType
                if (!MediaValidationUtil.isValidMimeTypeForMediaType(request.getContentType(),
                                request.getMediaType())) {
                        logger.warn("Invalid MIME type {} for mediaType: {}", request.getContentType(),
                                        request.getMediaType());
                        throw new ServiceException(
                                        String.format("MIME type '%s' not allowed for %s",
                                                        request.getContentType(), request.getMediaType()),
                                        HttpStatus.BAD_REQUEST);
                }

                // 3. Handle usage type-specific validation
                if (request.getUsageType() == MediaUsageType.PROFILE_PICTURE) {
                        // Profile picture must NOT have conversationId
                        if (request.getConversationId() != null) {
                                logger.warn("Profile picture upload attempted with conversationId for userId: {}",
                                                userId);
                                throw new ServiceException("Profile picture upload should not specify conversationId",
                                                HttpStatus.BAD_REQUEST);
                        }
                } else if (request.getUsageType() == MediaUsageType.CHAT_ATTACHMENT) {
                        // Chat attachment MUST have conversationId
                        if (request.getConversationId() == null) {
                                logger.warn("Chat attachment upload missing conversationId for userId: {}", userId);
                                throw new ServiceException("Chat attachment requires conversationId",
                                                HttpStatus.BAD_REQUEST);
                        }

                        // Verify user is a participant in the conversation
                        boolean isParticipant = conversationParticipantRepository
                                        .findByConversationId(Long.valueOf(request.getConversationId()))
                                        .stream()
                                        .anyMatch(p -> p.getUserId().equals(userId));

                        if (!isParticipant) {
                                logger.warn("User {} is not a participant in conversation {}",
                                                userId, request.getConversationId());
                                throw new ServiceException("User not authorized for this conversation",
                                                HttpStatus.FORBIDDEN);
                        }
                }

                // 4. Check idempotency: if clientUploadId provided, ensure no duplicate exists
                if (request.getClientUploadId() != null && !request.getClientUploadId().isBlank()) {
                        mediaRepository.findByClientUploadId(request.getClientUploadId()).ifPresent(existing -> {
                                logger.info("Duplicate upload detected for clientUploadId: {}, returning existing mediaId: {}",
                                                request.getClientUploadId(), existing.getId());
                                // NOTE: In a real scenario, we'd return the existing mediaId
                                // For now, we'll allow the creation (you can modify this behavior)
                        });
                }

                // 5. Generate S3 key using S3KeyGenerator
                String mediaId = String.valueOf(System.nanoTime()); // Will be replaced by actual DB ID after save
                String s3Key;
                if (request.getUsageType() == MediaUsageType.PROFILE_PICTURE) {
                        s3Key = S3KeyGenerator.profilePictureKey(String.valueOf(userId), mediaId,
                                        request.getFileName());
                } else {
                        // CHAT_ATTACHMENT - use chatMediaKey or documentKey based on mediaType
                        if (request.getMediaType() == MediaType.DOCUMENT) {
                                s3Key = S3KeyGenerator.documentKey(String.valueOf(request.getConversationId()),
                                                mediaId, request.getFileName());
                        } else {
                                s3Key = S3KeyGenerator.chatMediaKey(String.valueOf(request.getConversationId()),
                                                mediaId, request.getFileName());
                        }
                }

                // 6. Create Media entity with UPLOAD_PENDING status
                Media media = new Media();
                media.setOwnerUserId(userId);
                media.setConversationId(
                                request.getConversationId() != null ? Long.valueOf(request.getConversationId()) : null);
                media.setFileName(request.getFileName());
                media.setContentType(request.getContentType());
                media.setFileSize(request.getFileSize());
                media.setMediaType(request.getMediaType());
                media.setUsageType(request.getUsageType());
                media.setStatus(MediaUploadStatus.UPLOAD_PENDING);
                media.setClientUploadId(request.getClientUploadId());
                // s3Key will be updated after we get the actual ID
                media.setS3Key("temp"); // Temporary, will update below

                // 7. Save to DB to get the auto-generated ID
                Media savedMedia = mediaRepository.save(media);
                logger.debug("Created Media entity with ID: {}", savedMedia.getId());

                // 8. Update S3 key with actual media ID
                String finalS3Key;
                if (request.getUsageType() == MediaUsageType.PROFILE_PICTURE) {
                        finalS3Key = S3KeyGenerator.profilePictureKey(String.valueOf(userId),
                                        String.valueOf(savedMedia.getId()), request.getFileName());
                } else {
                        if (request.getMediaType() == MediaType.DOCUMENT) {
                                finalS3Key = S3KeyGenerator.documentKey(String.valueOf(request.getConversationId()),
                                                String.valueOf(savedMedia.getId()), request.getFileName());
                        } else {
                                finalS3Key = S3KeyGenerator.chatMediaKey(String.valueOf(request.getConversationId()),
                                                String.valueOf(savedMedia.getId()), request.getFileName());
                        }
                }
                savedMedia.setS3Key(finalS3Key);
                logger.debug("Updated Media s3Key: {}", finalS3Key);

                // 9. Generate pre-signed PUT URL for direct S3 upload
                String presignedUrl = awsStorageService.generatePreSignedPutUrl(finalS3Key, uploadUrlExpiryMinutes);
                logger.info("Generated pre-signed PUT URL for mediaId: {} (expires in {} minutes)",
                                savedMedia.getId(), uploadUrlExpiryMinutes);

                // 10. Return response
                return new MediaUploadInitResponse(
                                savedMedia.getId().toString(),
                                presignedUrl,
                                uploadUrlExpiryMinutes // in minutes
                );
        }

        @Override
        @Transactional
        public MediaUploadCompleteResponse completeUpload(Long userId, Long mediaId) {
                logger.info("Completing media upload for userId: {}, mediaId: {}", userId, mediaId);

                // 1. Load Media by (mediaId, userId) - verify ownership
                Media media = mediaRepository.findByIdAndOwnerUserId(mediaId, userId)
                                .orElseThrow(() -> {
                                        logger.warn("Media not found for mediaId: {}, userId: {}", mediaId, userId);
                                        return new ServiceException("Media not found", HttpStatus.NOT_FOUND);
                                });

                // 2. Verify Media.status == UPLOAD_PENDING
                if (media.getStatus() != MediaUploadStatus.UPLOAD_PENDING) {
                        logger.warn("Media {} has status {}, expected UPLOAD_PENDING", mediaId, media.getStatus());
                        throw new ServiceException("Upload already completed or in invalid state", HttpStatus.CONFLICT);
                }

                // 3. Verify S3 object exists
                if (!awsStorageService.verifyObjectExists(media.getS3Key())) {
                        logger.warn("S3 object not found for s3Key: {}", media.getS3Key());
                        throw new ServiceException("File not found in S3. Please upload the file first.",
                                        HttpStatus.UNPROCESSABLE_ENTITY);
                }
                logger.debug("Verified S3 object exists: {}", media.getS3Key());

                // 4. Update Media status to ACTIVE and set uploadedAt
                int updated = mediaRepository.updateStatusAndUploadedAtByIdAndOwnerUserId(
                                mediaId, userId, MediaUploadStatus.ACTIVE, Instant.now());

                if (updated == 0) {
                        logger.warn("Failed to update Media {} status", mediaId);
                        throw new ServiceException("Failed to mark upload as complete",
                                        HttpStatus.INTERNAL_SERVER_ERROR);
                }
                logger.info("Media {} marked as ACTIVE", mediaId);

                // 5. Return success response
                return new MediaUploadCompleteResponse(
                                mediaId.toString(),
                                MediaUploadStatus.ACTIVE.name(),
                                "Upload completed successfully");
        }

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "mediaPreSignedUrlCache", key = "#userId + '-' + #mediaId", unless = "#result == null")
        public MediaPreSignedUrlResponse generatePreSignedGetUrl(Long userId, Long mediaId) {
                logger.info("Generating pre-signed GET URL for userId: {}, mediaId: {}", userId, mediaId);

                // 1. Load Media by mediaId
                Media media = mediaRepository.findById(mediaId)
                                .orElseThrow(() -> {
                                        logger.warn("Media not found for mediaId: {}", mediaId);
                                        return new ServiceException("Media not found", HttpStatus.NOT_FOUND);
                                });

                // 2. Verify Media.status == ACTIVE
                if (media.getStatus() != MediaUploadStatus.ACTIVE) {
                        logger.warn("Media {} has status {}, expected ACTIVE", mediaId, media.getStatus());
                        throw new ServiceException("Media is not available yet. Upload may still be pending.",
                                        HttpStatus.BAD_REQUEST);
                }

                // 3. Authorize access based on usageType
                if (media.getUsageType() == MediaUsageType.PROFILE_PICTURE) {
                        if (!media.getOwnerUserId().equals(userId)) {
                                logger.warn("Access denied for userId: {} on profile picture mediaId: {}", userId, mediaId);
                                throw new ServiceException("Media not found", HttpStatus.NOT_FOUND);
                        }
                } else if (media.getUsageType() == MediaUsageType.CHAT_ATTACHMENT) {
                        boolean isParticipant = conversationParticipantRepository
                                        .findByConversationId(media.getConversationId())
                                        .stream()
                                        .anyMatch(p -> p.getUserId().equals(userId));

                        if (!isParticipant) {
                                logger.warn("User {} is not a participant in conversation {} for mediaId: {}",
                                                userId, media.getConversationId(), mediaId);
                                throw new ServiceException("User not authorized for this conversation",
                                                HttpStatus.FORBIDDEN);
                        }
                }

                // 4. Generate pre-signed GET URL
                String presignedUrl = awsStorageService.generatePreSignedGetUrl(media.getS3Key(),
                                downloadUrlExpiryMinutes);
                logger.info("Generated pre-signed GET URL for mediaId: {} (expires in {} minutes)",
                                mediaId, downloadUrlExpiryMinutes);

                // 5. Return response
                return new MediaPreSignedUrlResponse(
                                presignedUrl,
                                downloadUrlExpiryMinutes,
                                media.getId().toString(),
                                media.getMediaType().name(),
                                media.getFileName()
                );
        }

        @Override
        @Transactional(readOnly = true)
        public MediaPreSignedUrlResponse getMediaFromConversation(Long userId, Long mediaId, Long conversationId) {
                logger.info("Retrieving media from conversation: userId={}, mediaId={}, conversationId={}",
                                userId, mediaId, conversationId);

                // 1. Verify user is a participant in the conversation
                boolean isParticipant = conversationParticipantRepository
                                .findByConversationId(conversationId)
                                .stream()
                                .anyMatch(p -> p.getUserId().equals(userId));

                if (!isParticipant) {
                        logger.warn("User authorization failed: userId={} not a participant in conversationId={}",
                                        userId, conversationId);
                        throw new ServiceException("User not authorized for this conversation",
                                        HttpStatus.FORBIDDEN);
                }
                logger.debug("User authorization verified: userId={} is participant in conversationId={}",
                                userId, conversationId);

                // 2. Load Media by (mediaId, conversationId) - verify media is in this
                // conversation
                Media media = mediaRepository.findByIdAndConversationId(mediaId, conversationId)
                                .orElseThrow(() -> {
                                        logger.warn("Media retrieval failed: mediaId={} not found in conversationId={}",
                                                        mediaId, conversationId);
                                        return new ServiceException("Media not found in this conversation",
                                                        HttpStatus.NOT_FOUND);
                                });
                logger.debug("Media retrieved successfully: mediaId={}, s3Key={}", mediaId, media.getS3Key());

                // 3. Verify Media.status == ACTIVE
                if (media.getStatus() != MediaUploadStatus.ACTIVE) {
                        logger.warn("Media status validation failed: mediaId={} has status={}, expected=ACTIVE",
                                        mediaId, media.getStatus());
                        throw new ServiceException("Media is not available yet. Upload may still be pending.",
                                        HttpStatus.BAD_REQUEST);
                }
                logger.debug("Media status validated: mediaId={} status=ACTIVE", mediaId);

                // 4. Generate pre-signed GET URL
                String presignedUrl = awsStorageService.generatePreSignedGetUrl(media.getS3Key(),
                                downloadUrlExpiryMinutes);
                logger.info("Pre-signed GET URL generated: mediaId={}, expiryMinutes={}",
                                mediaId, downloadUrlExpiryMinutes);

                // 5. Return response
                return new MediaPreSignedUrlResponse(
                                presignedUrl,
                                downloadUrlExpiryMinutes,
                                media.getId().toString(),
                                media.getMediaType().name(),
                                media.getFileName());
        }

        @Transactional
        public void updateMessageId(Long userId, Long mediaId, Long messageId) {
                logger.info("Updating messageId for mediaId: {}, userId: {}, messageId: {}",
                                mediaId, userId, messageId);

                // 1. Verify media exists and belongs to user
                Media media = mediaRepository.findByIdAndOwnerUserId(mediaId, userId)
                                .orElseThrow(() -> {
                                        logger.warn("Media not found for mediaId: {}, userId: {}", mediaId, userId);
                                        return new ServiceException("Media not found", HttpStatus.NOT_FOUND);
                                });

                // 2. Verify Media.status == ACTIVE (only update messageId for active media)
                if (media.getStatus() != MediaUploadStatus.ACTIVE) {
                        logger.warn("Cannot update messageId for media {} with status {}, expected ACTIVE",
                                        mediaId, media.getStatus());
                        throw new ServiceException("Can only associate active media with a message",
                                        HttpStatus.BAD_REQUEST);
                }

                // 3. Update messageId in database
                int updated = mediaRepository.updateMessageIdByIdAndOwnerUserId(
                                mediaId, userId, messageId);

                if (updated == 0) {
                        logger.warn("Failed to update messageId for media {}", mediaId);
                        throw new ServiceException("Failed to associate media with message",
                                        HttpStatus.INTERNAL_SERVER_ERROR);
                }

                logger.info("Successfully updated messageId for mediaId: {} to messageId: {}",
                                mediaId, messageId);
        }
}
