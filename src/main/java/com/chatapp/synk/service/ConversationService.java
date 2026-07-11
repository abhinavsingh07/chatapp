package com.chatapp.synk.service;

import com.chatapp.synk.dto.ConversationDTO;

public interface ConversationService {
    ConversationDTO createConversation(ConversationDTO dto);

    ConversationDTO getConversationById(String id);

    String getOrCreateConversation(String userId, String contactUserId);
}
