package com.chatapp.synk.mediaUpload.util;

import com.chatapp.synk.mediaUpload.enums.MediaType;

/**
 * Utility class for validating media uploads.
 * Provides MIME type validation and file size limits per media type.
 */
public final class MediaValidationUtil {

    private MediaValidationUtil() {
        // Prevent instantiation
    }

    // File size limits (in bytes)
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final long MAX_VIDEO_SIZE = 50 * 1024 * 1024; // 50 MB
    private static final long MAX_DOCUMENT_SIZE = 20 * 1024 * 1024; // 20 MB

    /**
     * Get maximum allowed file size for a given media type.
     *
     * @param mediaType Type of media (IMAGE, VIDEO, DOCUMENT)
     * @return Maximum file size in bytes
     */
    public static long getMaxFileSizeForMediaType(MediaType mediaType) {
        return switch (mediaType) {
            case IMAGE -> MAX_IMAGE_SIZE;
            case VIDEO -> MAX_VIDEO_SIZE;
            case DOCUMENT -> MAX_DOCUMENT_SIZE;
            default -> throw new IllegalArgumentException("Unknown media type: " + mediaType);
        };
    }

    /**
     * Validate if a MIME type is allowed for a given media type.
     *
     * @param mimeType  Content-Type/MIME type (e.g., "image/jpeg", "video/mp4", "application/pdf")
     * @param mediaType Media type enum (IMAGE, VIDEO, DOCUMENT)
     * @return true if MIME type is allowed for the media type, false otherwise
     */
    public static boolean isValidMimeTypeForMediaType(String mimeType, MediaType mediaType) {
        if (mimeType == null || mimeType.isBlank()) {
            return false;
        }

        String normalizedMimeType = mimeType.toLowerCase().trim();

        return switch (mediaType) {
            case IMAGE -> isValidImageMimeType(normalizedMimeType);
            case VIDEO -> isValidVideoMimeType(normalizedMimeType);
            case DOCUMENT -> isValidDocumentMimeType(normalizedMimeType);
            default -> false;
        };
    }

    /**
     * Check if MIME type is a valid image type.
     * Allowed: image/jpeg, image/png, image/gif, image/webp, image/bmp
     * Excluded: image/svg+xml (security risk for XSS)
     *
     * @param mimeType Normalized MIME type (lowercase)
     * @return true if valid image MIME type
     */
    private static boolean isValidImageMimeType(String mimeType) {
        return mimeType.equals("image/jpeg") ||
               mimeType.equals("image/jpg") ||
               mimeType.equals("image/png") ||
               mimeType.equals("image/gif") ||
               mimeType.equals("image/webp") ||
               mimeType.equals("image/bmp");
    }

    /**
     * Check if MIME type is a valid video type.
     * Allowed: video/mp4, video/mpeg, video/quicktime, video/webm, video/x-msvideo
     *
     * @param mimeType Normalized MIME type (lowercase)
     * @return true if valid video MIME type
     */
    private static boolean isValidVideoMimeType(String mimeType) {
        return mimeType.equals("video/mp4") ||
               mimeType.equals("video/mpeg") ||
               mimeType.equals("video/quicktime") ||
               mimeType.equals("video/webm") ||
               mimeType.equals("video/x-msvideo") ||
               mimeType.equals("video/x-matroska") ||
               mimeType.equals("video/3gpp");
    }

    /**
     * Check if MIME type is a valid document type.
     * Allowed: application/pdf, Word docs, Excel, PowerPoint, text/plain
     * Excluded: Executable files, scripts
     *
     * @param mimeType Normalized MIME type (lowercase)
     * @return true if valid document MIME type
     */
    private static boolean isValidDocumentMimeType(String mimeType) {
        return mimeType.equals("application/pdf") ||
               // MS Word
               mimeType.equals("application/msword") ||
               mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
               // MS Excel
               mimeType.equals("application/vnd.ms-excel") ||
               mimeType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
               // MS PowerPoint
               mimeType.equals("application/vnd.ms-powerpoint") ||
               mimeType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation") ||
               // Text files
               mimeType.equals("text/plain") ||
               // OpenDocument formats
               mimeType.equals("application/vnd.oasis.opendocument.text") ||
               mimeType.equals("application/vnd.oasis.opendocument.spreadsheet") ||
               mimeType.equals("application/vnd.oasis.opendocument.presentation");
    }
}
