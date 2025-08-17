package com.chatapp.synk.service;

import com.chatapp.synk.dto.MessageDTO;

import java.util.List;

public interface MessageService {
    List<MessageDTO> getMessagesByConversationId(String conversationId);
    List<MessageDTO> getUnreadMessagesForReceiver(String receiverId);
    MessageDTO sendMessage(MessageDTO messageDTO);
    void markMessageAsRead(String messageId);
}