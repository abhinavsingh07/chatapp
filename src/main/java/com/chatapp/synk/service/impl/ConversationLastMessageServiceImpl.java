package com.chatapp.synk.service.impl;

import com.chatapp.synk.dto.ConversationLastMsgDTO;
import com.chatapp.synk.mediaUpload.repository.MediaRepository;
import com.chatapp.synk.repository.ConversationLastMessageRepository;
import com.chatapp.synk.security_validator.InputSecurityUtils;
import com.chatapp.synk.service.ConversationLastMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ConversationLastMessageServiceImpl implements ConversationLastMessageService {
    private static final Logger logger = LoggerFactory.getLogger(ConversationLastMessageServiceImpl.class);

    private final ConversationLastMessageRepository conversationLastMessageRepository;
    private final MediaRepository mediaRepository;

    public ConversationLastMessageServiceImpl(ConversationLastMessageRepository conversationLastMessageRepository,
            MediaRepository mediaRepository) {
        this.conversationLastMessageRepository = conversationLastMessageRepository;
        this.mediaRepository = mediaRepository;
    }

    @Override
    public void upsertLastMessage(String conversationId, String messageId, String senderId, String content) {
        String conversationValidId = InputSecurityUtils.secureId(conversationId);
        String messageValidId = InputSecurityUtils.secureId(messageId);
        String senderValidId = InputSecurityUtils.secureId(senderId);
        String safeContent = InputSecurityUtils.secureMessage(content);
        Instant sentAt = Instant.now();
        Instant updatedAt = Instant.now();

        logger.info("Upserting last message for conversationId={}, messageId={}", conversationValidId, messageValidId);

       //db call to upsert last message
        conversationLastMessageRepository.upsertLastMessage(Long.parseLong(conversationValidId),
                Long.parseLong(messageValidId), Long.parseLong(senderValidId),
                safeContent, sentAt, updatedAt);
        if (logger.isDebugEnabled()) {
            logger.debug("Successfully upserted last message for conversationId={}", conversationValidId);
        }
    }

    @Override
    public List<ConversationLastMsgDTO> findUserConversations(String userId) {
        String validUserId = InputSecurityUtils.secureId(userId);
        logger.info("Fetching chat list for loggedInUserId={}", validUserId);
        
        List<ConversationLastMsgDTO> chatList = conversationLastMessageRepository
                .findUserConversations(Long.parseLong(validUserId));

        // Batch fetch latest active profile pictures — single query instead of N
        List<Long> userIds = chatList.stream()
                .map(dto -> Long.parseLong(dto.getUserId()))
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Long> mediaIdByUserId = new HashMap<>();
        if (!userIds.isEmpty()) {
            mediaRepository.findLatestActiveProfilePictureIds(userIds)
                    .forEach(row -> {
                        Long ownerUserId = (Long) row[0];
                        Long mediaId = (Long) row[1];
                        mediaIdByUserId.putIfAbsent(ownerUserId, mediaId); // first = latest (DESC sorted)
                    });
        }

        for (ConversationLastMsgDTO dto : chatList) {
            Long mediaId = mediaIdByUserId.get(Long.parseLong(dto.getUserId()));
            dto.setMediaId(mediaId != null ? String.valueOf(mediaId) : null);
        }

        if(logger.isDebugEnabled()) {
            logger.debug("Fetched {} conversations for userId={}", chatList.size(), validUserId);
        }
        return chatList;
    }
}
