package com.chatapp.synk.mediaUpload.controller;

import com.chatapp.synk.exceptionHandler.ServiceException;
import com.chatapp.synk.mediaUpload.dto.MediaPreSignedUrlResponse;
import com.chatapp.synk.mediaUpload.dto.MediaUploadCompleteResponse;
import com.chatapp.synk.mediaUpload.dto.MediaUploadInitRequest;
import com.chatapp.synk.mediaUpload.dto.MediaUploadInitResponse;
import com.chatapp.synk.mediaUpload.service.MediaUploadService;
import com.chatapp.synk.response.SuccessResponse;
import com.chatapp.synk.security.SecurityUtil;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for media upload operations.
 * Provides endpoints for:
 * - Initiating media uploads (get pre-signed URLs)
 * - Completing media uploads (after S3 verification)
 * - Downloading media (get pre-signed download URLs)
 */
@RestController
@RequestMapping("/media")
public class MediaController {

    private static final Logger logger = LoggerFactory.getLogger(MediaController.class);

    private final MediaUploadService mediaUploadService;

    public MediaController(MediaUploadService mediaUploadService) {
        this.mediaUploadService = mediaUploadService;
    }

    /**
     * Initiate a media upload session.
     * Validates file type/size, creates Media DB record (UPLOAD_PENDING), returns
     * pre-signed PUT URL.
     * Frontend uses the returned URL to upload file directly to S3.
     *
     * @param request Upload initialization request with file metadata
     * @return SuccessResponse containing mediaId and presigned upload URL
     * @throws ServiceException if validation fails or user unauthorized
     */
    @PostMapping("/upload-init")
    public ResponseEntity<SuccessResponse<MediaUploadInitResponse>> initiateUpload(
            @Valid @RequestBody MediaUploadInitRequest request) {

        Long userId = Long.parseLong(SecurityUtil.getCurrentUserIdFromSecurityContext());
        logger.info("Upload init request received for userId: {}, mediaType: {}, usageType: {}",
                userId, request.getMediaType(), request.getUsageType());

        MediaUploadInitResponse response = mediaUploadService.initiateUpload(userId, request);

        logger.debug("Upload session initiated: mediaId: {}", response.getMediaId());
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK,
                "Upload initiated. Use the presigned URL to upload file directly to S3.",
                List.of(response)));
    }

    /**
     * Complete a media upload after file has been uploaded to S3.
     * Verifies S3 object exists, then marks Media status as ACTIVE.
     * Call this endpoint AFTER uploading file to S3 using the pre-signed URL.
     *
     * @param mediaId ID of the media record to mark complete
     * @return SuccessResponse with upload completion status
     * @throws ServiceException if media not found, S3 verification fails, etc.
     */
    @PostMapping("/upload-complete/{mediaId}")
    public ResponseEntity<SuccessResponse<MediaUploadCompleteResponse>> completeUpload(
            @PathVariable Long mediaId) {
        Long userId = Long.parseLong(SecurityUtil.getCurrentUserIdFromSecurityContext());
        logger.info("Upload complete request for mediaId: {}, userId: {}", mediaId, userId);

        MediaUploadCompleteResponse response = mediaUploadService.completeUpload(userId, mediaId);

        logger.info("Media {} marked as ACTIVE", mediaId);
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK,
                "Upload completed successfully. Media is now available for use.",
                List.of(response)));

    }

    /**
     * Generate a pre-signed GET URL for downloading media from S3.
     * Used by frontend to access/download previously uploaded media.
     * URL is valid for 15 minutes.
     *
     * @param mediaId ID of the media to download (query parameter)
     * @return SuccessResponse containing presigned download URL
     * @throws ServiceException if media not found, not ACTIVE, or user unauthorized
     */
    @GetMapping("/pre-signed-url/{mediaId}")
    public ResponseEntity<SuccessResponse<MediaPreSignedUrlResponse>> getPreSignedUrl(
            @PathVariable Long mediaId) {

        Long userId = Long.parseLong(SecurityUtil.getCurrentUserIdFromSecurityContext());
        logger.debug("Pre-signed download URL request for mediaId: {}, userId: {}", mediaId, userId);

        MediaPreSignedUrlResponse response = mediaUploadService.generatePreSignedGetUrl(userId, mediaId);

        logger.debug("Pre-signed GET URL generated for mediaId: {}", mediaId);
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK,
                "Pre-signed download URL generated. URL expires in 15 minutes.",
                List.of(response)));
    }
}
