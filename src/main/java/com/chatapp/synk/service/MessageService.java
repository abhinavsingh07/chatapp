package com.chatapp.synk.service;

import com.chatapp.synk.dto.MessageDTO;

import java.util.List;

public interface MessageService {
    List<MessageDTO> getMessagesByConversationId(String conversationId);

    List<MessageDTO> getUnreadMessagesForReceiver(String conversationId, String receiverId);

    MessageDTO saveMessage(MessageDTO messageDTO);

    void markMessageAsRead(String messageId);

    /**
     * Atomically save a message and associate media files with it.
     * Both operations are performed in a single transaction.
     *
     * @param messageDTO The message to save
     * @param mediaIdsStr Semicolon-separated media IDs (e.g., "1;2;3")
     * @param fromUserId The user ID of the message sender (media owner)
     */
    void saveMessageWithMediaIds(MessageDTO messageDTO, String mediaIdsStr, Long fromUserId);
}