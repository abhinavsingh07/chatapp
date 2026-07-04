package com.chatapp.synk.mediaUpload.util;

public final class S3KeyGenerator {

    private S3KeyGenerator() {
    }

    /**
     * Generates S3 key path for user profile picture storage.
     */
    public static String profilePictureKey(String userId, String mediaId, String originalFileName) {
        String extension = extractExtension(originalFileName);
        return "profile-pictures/%s/%s%s".formatted(userId, mediaId, extension);
    }

    /**
     * Generates S3 key path for chat media storage within a conversation.
     */
    public static String chatMediaKey(String conversationId, String mediaId, String originalFileName) {
        String extension = extractExtension(originalFileName);
        return "chat-media/%s/%s%s".formatted(conversationId, mediaId, extension);
    }

    /**
     * Generates S3 key path for document storage within a conversation.
     */
    public static String documentKey(String conversationId, String mediaId, String originalFileName) {
        String extension = extractExtension(originalFileName);
        return "documents/%s/%s%s".formatted(conversationId, mediaId, extension);
    }

    /**
     * Extracts and normalizes file extension from filename (e.g., "photo.jpg" → ".jpg").
     */
    private static String extractExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "";
        }

        String cleanFileName = fileName.trim();

        int lastDotIndex = cleanFileName.lastIndexOf(".");

        if (lastDotIndex == -1 || lastDotIndex == cleanFileName.length() - 1) {
            return "";
        }

        return cleanFileName.substring(lastDotIndex).toLowerCase();
    }
}