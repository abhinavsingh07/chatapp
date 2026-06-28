package com.chatapp.synk.dto;

import java.time.Instant;

public class ConversationLastMsgDTO {
    // ConversationLastMessage fields
    private String messageId;
    private String conversationId;
    private String content;
    private String sentAt;
    private String senderId;
    // Conversation field
    private String conversationType;
    // User fields
    private String userId;
    private String userName;
    private String userProfilePictureUrl;

    // private String updatedAt;

    public ConversationLastMsgDTO(Long messageId, Long conversationId, String content, Instant sentAt,
            Long senderId, String conversationType, Long userId, String userName, String userProfilePictureUrl) {
        this.messageId = String.valueOf(messageId);
        this.conversationId = String.valueOf(conversationId);
        this.content = content;
        this.sentAt = sentAt.toString();
        this.senderId = String.valueOf(senderId);
        this.conversationType = conversationType;
        this.userId = String.valueOf(userId);
        this.userName = userName;
        this.userProfilePictureUrl = userProfilePictureUrl;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSentAt() {
        return sentAt;
    }

    public void setSentAt(String sentAt) {
        this.sentAt = sentAt;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getConversationType() {
        return conversationType;
    }

    public void setConversationType(String conversationType) {
        this.conversationType = conversationType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserProfilePictureUrl() {
        return userProfilePictureUrl;
    }

    public void setUserProfilePictureUrl(String userProfilePictureUrl) {
        this.userProfilePictureUrl = userProfilePictureUrl;
    }

}