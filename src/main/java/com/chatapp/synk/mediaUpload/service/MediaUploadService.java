package com.chatapp.synk.mediaUpload.service;

import com.chatapp.synk.mediaUpload.dto.MediaUploadInitRequest;
import com.chatapp.synk.mediaUpload.dto.MediaUploadInitResponse;
import com.chatapp.synk.mediaUpload.dto.MediaUploadCompleteResponse;
import com.chatapp.synk.mediaUpload.dto.MediaPreSignedUrlResponse;

public interface MediaUploadService {

    /**
     * Initiate a media upload session.
     * Validates file type, size, user permissions, and creates a Media DB row with
     * UPLOAD_PENDING status.
     * Returns pre-signed PUT URL for direct S3 upload.
     *
     * @param userId  User initiating the upload
     * @param request Upload initialization request with file metadata
     * @return Response containing mediaId and presigned PUT URL
     * @throws com.chatapp.synk.response.ServiceException if validation fails or
     *                                                    user unauthorized
     */
    MediaUploadInitResponse initiateUpload(Long userId, MediaUploadInitRequest request);

    /**
     * Complete a media upload after file has been uploaded to S3.
     * Verifies S3 object exists, then marks Media status as ACTIVE.
     *
     * @param userId  User who initiated the upload
     * @param mediaId ID of the media record to mark complete
     * @return Response with updated media status
     * @throws com.chatapp.synk.response.ServiceException if media not found,
     *                                                    verification fails, etc.
     */
    MediaUploadCompleteResponse completeUpload(Long userId, Long mediaId);

    /**
     * Generate a pre-signed GET URL for downloading media from S3.
     * Used by frontend to access/download previously uploaded media.
     *
     * @param userId  User requesting the download URL
     * @param mediaId ID of the media to download
     * @return Response containing presigned GET URL with 15-min expiry
     * @throws com.chatapp.synk.response.ServiceException if media not found, not
     *                                                    ACTIVE, or user
     *                                                    unauthorized
     */
    MediaPreSignedUrlResponse generatePreSignedGetUrl(Long userId, Long mediaId);

    public MediaPreSignedUrlResponse getMediaFromConversation(Long userId, Long mediaId, Long conversationId);

    /**
     * Associate a media record with a message after the message is saved.
     * Verifies media exists, belongs to the user, and is ACTIVE before updating.
     *
     * @param userId    Owner of the media
     * @param mediaId   ID of the media record
     * @param messageId ID of the message to associate
     * @throws com.chatapp.synk.exceptionHandler.ServiceException if media not found,
     *                                                            not ACTIVE, or update fails
     */
    void updateMessageId(Long userId, Long mediaId, Long messageId);
}
