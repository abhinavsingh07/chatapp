package com.chatapp.synk.controller;

import com.chatapp.synk.mediaUpload.dto.MediaUploadInitRequest;
import com.chatapp.synk.mediaUpload.dto.MediaUploadInitResponse;
import com.chatapp.synk.mediaUpload.dto.MediaUploadCompleteResponse;
import com.chatapp.synk.mediaUpload.controller.MediaController;
import com.chatapp.synk.mediaUpload.dto.MediaPreSignedUrlResponse;
import com.chatapp.synk.mediaUpload.enums.MediaType;
import com.chatapp.synk.mediaUpload.enums.MediaUploadStatus;
import com.chatapp.synk.mediaUpload.enums.MediaUsageType;
import com.chatapp.synk.mediaUpload.service.MediaUploadService;
import com.chatapp.synk.response.SuccessResponse;
import com.chatapp.synk.security.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaControllerTest {

        private static final String TEST_USER_ID = "12345";

        @Mock
        private MediaUploadService mediaUploadService;

        @InjectMocks
        private MediaController mediaController;

        private MediaUploadInitRequest uploadInitRequest;
        private MediaUploadInitResponse uploadInitResponse;
        private MediaUploadCompleteResponse completeResponse;
        private MediaPreSignedUrlResponse preSignedUrlResponse;

        @BeforeEach
        void setUp() {
                // Mock upload init request
                uploadInitRequest = new MediaUploadInitRequest();
                uploadInitRequest.setMediaType(MediaType.IMAGE);
                uploadInitRequest.setUsageType(MediaUsageType.PROFILE_PICTURE);
                uploadInitRequest.setContentType("image/jpeg");
                uploadInitRequest.setFileSize(5242880L); // 5 MB
                uploadInitRequest.setFileName("profile.jpg");
                uploadInitRequest.setClientUploadId("client-123");

                // Mock upload init response
                uploadInitResponse = new MediaUploadInitResponse(
                                "1",
                                "https://s3.amazonaws.com/synk-media-bucket/presigned-put-url",
                                600 // 10 minutes in seconds
                );

                // Mock upload complete response
                completeResponse = new MediaUploadCompleteResponse(
                                "1",
                                MediaUploadStatus.ACTIVE.name(),
                                "Upload completed successfully");

                // Mock pre-signed download URL response
                preSignedUrlResponse = new MediaPreSignedUrlResponse(
                                "https://s3.amazonaws.com/synk-media-bucket/presigned-get-url",
                                900 // 15 minutes in seconds
                );
        }

        // ==================== Upload Init Tests ====================

        @Test
        void testUploadInit_WithValidProfilePicture_Success() {
                try (var mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
                        mockedSecurityUtil.when(SecurityUtil::getCurrentUserIdFromSecurityContext)
                                        .thenReturn(TEST_USER_ID);

                        // Arrange
                        when(mediaUploadService.initiateUpload(anyLong(), any(MediaUploadInitRequest.class)))
                                        .thenReturn(uploadInitResponse);

                        // Act
                        ResponseEntity<SuccessResponse<MediaUploadInitResponse>> response = mediaController
                                        .initiateUpload(uploadInitRequest);

                        // Assert
                        assertNotNull(response.getBody());
                        assertTrue(response.getStatusCode().is2xxSuccessful());
                        assertEquals(HttpStatus.OK, response.getBody().getResponseCode());
                        assertEquals("Upload initiated. Use the presigned URL to upload file directly to S3.",
                                        response.getBody().getMessage());
                        assertNotNull(response.getBody().getData());
                        assertEquals("1", response.getBody().getData().get(0).getMediaId());
                        assertTrue(response.getBody().getData().get(0).getPresignedUploadUrl()
                                        .contains("presigned-put-url"));
                        assertEquals(600, response.getBody().getData().get(0).getUploadUrlExpiresInMinutes());
                        verify(mediaUploadService, times(1)).initiateUpload(anyLong(),
                                        any(MediaUploadInitRequest.class));
                }
        }

        @Test
        void testUploadInit_WithValidChatMedia_Success() {
                try (var mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
                        mockedSecurityUtil.when(SecurityUtil::getCurrentUserIdFromSecurityContext)
                                        .thenReturn(TEST_USER_ID);

                        // Arrange
                        MediaUploadInitRequest chatMediaRequest = new MediaUploadInitRequest();
                        chatMediaRequest.setMediaType(MediaType.VIDEO);
                        chatMediaRequest.setUsageType(MediaUsageType.CHAT_ATTACHMENT);
                        chatMediaRequest.setContentType("video/mp4");
                        chatMediaRequest.setFileSize(20971520L); // 20 MB
                        chatMediaRequest.setFileName("video.mp4");
                        chatMediaRequest.setConversationId("1L");

                        when(mediaUploadService.initiateUpload(anyLong(), any(MediaUploadInitRequest.class)))
                                        .thenReturn(uploadInitResponse);

                        // Act
                        ResponseEntity<SuccessResponse<MediaUploadInitResponse>> response = mediaController
                                        .initiateUpload(chatMediaRequest);

                        // Assert
                        assertNotNull(response.getBody());
                        assertTrue(response.getStatusCode().is2xxSuccessful());
                        assertEquals(HttpStatus.OK, response.getBody().getResponseCode());
                        verify(mediaUploadService, times(1)).initiateUpload(anyLong(),
                                        any(MediaUploadInitRequest.class));
                }
        }

        @Test
        void testUploadInit_WithInvalidFileSize_Failure() {
                try (var mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
                        mockedSecurityUtil.when(SecurityUtil::getCurrentUserIdFromSecurityContext)
                                        .thenReturn(TEST_USER_ID);

                        // Arrange
                        MediaUploadInitRequest oversizedRequest = new MediaUploadInitRequest();
                        oversizedRequest.setMediaType(MediaType.IMAGE);
                        oversizedRequest.setUsageType(MediaUsageType.PROFILE_PICTURE);
                        oversizedRequest.setContentType("image/jpeg");
                        oversizedRequest.setFileSize(11534336L); // 11 MB (exceeds 10 MB limit for images)
                        oversizedRequest.setFileName("large.jpg");

                        when(mediaUploadService.initiateUpload(anyLong(), any(MediaUploadInitRequest.class)))
                                        .thenThrow(new com.chatapp.synk.exceptionHandler.ServiceException(
                                                        "File size exceeds limit",
                                                        org.springframework.http.HttpStatus.BAD_REQUEST));

                        // Act & Assert
                        assertThrows(com.chatapp.synk.exceptionHandler.ServiceException.class,
                                        () -> mediaController.initiateUpload(oversizedRequest));
                        verify(mediaUploadService, times(1)).initiateUpload(anyLong(),
                                        any(MediaUploadInitRequest.class));
                }
        }

        @Test
        void testUploadInit_WithInvalidMimeType_Failure() {
                try (var mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
                        mockedSecurityUtil.when(SecurityUtil::getCurrentUserIdFromSecurityContext)
                                        .thenReturn(TEST_USER_ID);

                        // Arrange
                        MediaUploadInitRequest invalidMimeRequest = new MediaUploadInitRequest();
                        invalidMimeRequest.setMediaType(MediaType.IMAGE);
                        invalidMimeRequest.setUsageType(MediaUsageType.PROFILE_PICTURE);
                        invalidMimeRequest.setContentType("application/pdf");
                        invalidMimeRequest.setFileSize(1048576L);
                        invalidMimeRequest.setFileName("document.pdf");

                        when(mediaUploadService.initiateUpload(anyLong(), any(MediaUploadInitRequest.class)))
                                        .thenThrow(new com.chatapp.synk.exceptionHandler.ServiceException(
                                                        "Invalid MIME type for IMAGE",
                                                        org.springframework.http.HttpStatus.BAD_REQUEST));

                        // Act & Assert
                        assertThrows(com.chatapp.synk.exceptionHandler.ServiceException.class,
                                        () -> mediaController.initiateUpload(invalidMimeRequest));
                        verify(mediaUploadService, times(1)).initiateUpload(anyLong(),
                                        any(MediaUploadInitRequest.class));
                }
        }

        @Test
        void testUploadInit_WithProfilePictureAndConversationId_Failure() {
                try (var mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
                        mockedSecurityUtil.when(SecurityUtil::getCurrentUserIdFromSecurityContext)
                                        .thenReturn(TEST_USER_ID);

                        // Arrange
                        MediaUploadInitRequest invalidRequest = new MediaUploadInitRequest();
                        invalidRequest.setMediaType(MediaType.IMAGE);
                        invalidRequest.setUsageType(MediaUsageType.PROFILE_PICTURE);
                        invalidRequest.setContentType("image/jpeg");
                        invalidRequest.setFileSize(5242880L);
                        invalidRequest.setFileName("profile.jpg");
                        invalidRequest.setConversationId("1L"); // Profile picture should NOT have conversationId

                        when(mediaUploadService.initiateUpload(anyLong(), any(MediaUploadInitRequest.class)))
                                        .thenThrow(new com.chatapp.synk.exceptionHandler.ServiceException(
                                                        "Profile picture cannot have conversation ID",
                                                        org.springframework.http.HttpStatus.BAD_REQUEST));

                        // Act & Assert
                        assertThrows(com.chatapp.synk.exceptionHandler.ServiceException.class,
                                        () -> mediaController.initiateUpload(invalidRequest));
                }
        }

        // ==================== Upload Complete Tests ====================

        @Test
        void testUploadComplete_WithValidMediaId_Success() {
                try (var mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
                        mockedSecurityUtil.when(SecurityUtil::getCurrentUserIdFromSecurityContext)
                                        .thenReturn(TEST_USER_ID);

                        // Arrange
                        when(mediaUploadService.completeUpload(anyLong(), eq(1L)))
                                        .thenReturn(completeResponse);

                        // Act
                        ResponseEntity<SuccessResponse<MediaUploadCompleteResponse>> response = mediaController
                                        .completeUpload("1");

                        // Assert
                        assertNotNull(response.getBody());
                        assertTrue(response.getStatusCode().is2xxSuccessful());
                        assertEquals(HttpStatus.OK, response.getBody().getResponseCode());
                        assertEquals("Upload completed successfully. Media is now available for use.",
                                        response.getBody().getMessage());
                        assertNotNull(response.getBody().getData());
                        assertEquals("1", response.getBody().getData().get(0).getMediaId());
                        assertEquals(MediaUploadStatus.ACTIVE.name(), response.getBody().getData().get(0).getStatus());
                        verify(mediaUploadService, times(1)).completeUpload(anyLong(), eq(1L));
                }
        }

        @Test
        void testUploadComplete_WithNonExistentMediaId_Failure() {
                try (var mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
                        mockedSecurityUtil.when(SecurityUtil::getCurrentUserIdFromSecurityContext)
                                        .thenReturn(TEST_USER_ID);

                        // Arrange
                        when(mediaUploadService.completeUpload(anyLong(), eq(999L)))
                                        .thenThrow(new com.chatapp.synk.exceptionHandler.ServiceException(
                                                        "Media not found",
                                                        org.springframework.http.HttpStatus.NOT_FOUND));

                        // Act & Assert
                        assertThrows(com.chatapp.synk.exceptionHandler.ServiceException.class,
                                        () -> mediaController.completeUpload("999"));
                        verify(mediaUploadService, times(1)).completeUpload(anyLong(), eq(999L));
                }
        }

        @Test
        void testUploadComplete_WithS3ObjectNotFound_Failure() {
                try (var mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
                        mockedSecurityUtil.when(SecurityUtil::getCurrentUserIdFromSecurityContext)
                                        .thenReturn(TEST_USER_ID);

                        // Arrange
                        when(mediaUploadService.completeUpload(anyLong(), eq(1L)))
                                        .thenThrow(new com.chatapp.synk.exceptionHandler.ServiceException(
                                                        "File not found in S3",
                                                        org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY));

                        // Act & Assert
                        assertThrows(com.chatapp.synk.exceptionHandler.ServiceException.class,
                                        () -> mediaController.completeUpload("1"));
                        verify(mediaUploadService, times(1)).completeUpload(anyLong(), eq(1L));
                }
        }

        @Test
        void testUploadComplete_WithAlreadyCompletedUpload_Failure() {
                try (var mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
                        mockedSecurityUtil.when(SecurityUtil::getCurrentUserIdFromSecurityContext)
                                        .thenReturn(TEST_USER_ID);

                        // Arrange
                        when(mediaUploadService.completeUpload(anyLong(), eq(1L)))
                                        .thenThrow(new com.chatapp.synk.exceptionHandler.ServiceException(
                                                        "Upload already completed",
                                                        org.springframework.http.HttpStatus.CONFLICT));

                        // Act & Assert
                        assertThrows(com.chatapp.synk.exceptionHandler.ServiceException.class,
                                        () -> mediaController.completeUpload("1"));
                }
        }

        // ==================== Pre-Signed URL Tests ====================

        @Test
        void testGetPreSignedUrl_WithValidActiveMedia_Success() {

                // Arrange
                when(mediaUploadService.generatePreSignedGetUrl(anyLong(), eq(1L)))
                                .thenReturn(preSignedUrlResponse);

                // Act
                ResponseEntity<SuccessResponse<MediaPreSignedUrlResponse>> response = mediaController
                                .getPreSignedUrl("2", "1");

                // Assert
                assertNotNull(response.getBody());
                assertTrue(response.getStatusCode().is2xxSuccessful());
                assertEquals(HttpStatus.OK, response.getBody().getResponseCode());
                assertEquals("Pre-signed download URL generated. URL expires in 15 minutes.",
                                response.getBody().getMessage());
                assertNotNull(response.getBody().getData());
                assertTrue(response.getBody().getData().get(0).getPresignedDownloadUrl()
                                .contains("presigned-get-url"));
                assertEquals(900, response.getBody().getData().get(0).getDownloadUrlExpiresInMinutes());
                verify(mediaUploadService, times(1)).generatePreSignedGetUrl(anyLong(), eq(1L));

        }

        @Test
        void testGetPreSignedUrl_WithNonExistentMediaId_Failure() {

                // Arrange
                when(mediaUploadService.generatePreSignedGetUrl(anyLong(), eq(999L)))
                                .thenThrow(new com.chatapp.synk.exceptionHandler.ServiceException(
                                                "Media not found",
                                                org.springframework.http.HttpStatus.NOT_FOUND));

                // Act & Assert
                assertThrows(com.chatapp.synk.exceptionHandler.ServiceException.class,
                                () -> mediaController.getPreSignedUrl("2", "999"));
                verify(mediaUploadService, times(1)).generatePreSignedGetUrl(anyLong(), eq(999L));

        }

        @Test
        void testGetPreSignedUrl_WithPendingMedia_Failure() {

                // Arrange
                when(mediaUploadService.generatePreSignedGetUrl(anyLong(), eq(1L)))
                                .thenThrow(new com.chatapp.synk.exceptionHandler.ServiceException(
                                                "Media is not available yet. Upload may still be pending.",
                                                org.springframework.http.HttpStatus.BAD_REQUEST));

                // Act & Assert
                assertThrows(com.chatapp.synk.exceptionHandler.ServiceException.class,
                                () -> mediaController.getPreSignedUrl("2", "1"));
                verify(mediaUploadService, times(1)).generatePreSignedGetUrl(anyLong(), eq(1L));
        }

        @Test
        void testGetPreSignedUrl_WithUnauthorizedUser_Failure() {

                // Arrange
                when(mediaUploadService.generatePreSignedGetUrl(anyLong(), eq(1L)))
                                .thenThrow(new com.chatapp.synk.exceptionHandler.ServiceException(
                                                "Unauthorized to access this media",
                                                org.springframework.http.HttpStatus.FORBIDDEN));

                // Act & Assert
                assertThrows(com.chatapp.synk.exceptionHandler.ServiceException.class,
                                () -> mediaController.getPreSignedUrl("2", "1"));

        }

        // ==================== Idempotency Tests ====================

        @Test
        void testUploadInit_WithSameClientUploadId_ReturnsSameResponse() {
                try (var mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
                        mockedSecurityUtil.when(SecurityUtil::getCurrentUserIdFromSecurityContext)
                                        .thenReturn(TEST_USER_ID);

                        // Arrange
                        when(mediaUploadService.initiateUpload(anyLong(), any(MediaUploadInitRequest.class)))
                                        .thenReturn(uploadInitResponse);

                        // Act - First upload
                        ResponseEntity<SuccessResponse<MediaUploadInitResponse>> response1 = mediaController
                                        .initiateUpload(uploadInitRequest);

                        // Act - Second upload with same clientUploadId
                        ResponseEntity<SuccessResponse<MediaUploadInitResponse>> response2 = mediaController
                                        .initiateUpload(uploadInitRequest);

                        // Assert - Both should return same mediaId (idempotency)
                        assertEquals(response1.getBody().getData().get(0).getMediaId(),
                                        response2.getBody().getData().get(0).getMediaId());
                        verify(mediaUploadService, times(2)).initiateUpload(anyLong(),
                                        any(MediaUploadInitRequest.class));
                }
        }
}
