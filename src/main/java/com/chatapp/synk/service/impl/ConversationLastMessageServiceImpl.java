package com.chatapp.synk.service.impl;

import com.chatapp.synk.dto.ConversationLastMsgDTO;
import com.chatapp.synk.entity.Media;
import com.chatapp.synk.mediaUpload.enums.MediaUploadStatus;
import com.chatapp.synk.mediaUpload.enums.MediaUsageType;
import com.chatapp.synk.mediaUpload.repository.MediaRepository;
import com.chatapp.synk.repository.ConversationLastMessageRepository;
import com.chatapp.synk.security.SecurityUtil;
import com.chatapp.synk.security_validator.InputSecurityUtils;
import com.chatapp.synk.service.ConversationLastMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

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
        conversationLastMessageRepository.upsertLastMessage(Long.parseLong(conversationValidId),
                Long.parseLong(messageValidId), Long.parseLong(senderValidId),
                safeContent, sentAt, updatedAt);
        if (logger.isDebugEnabled()) {
            logger.debug("Successfully upserted last message for conversationId={}", conversationValidId);
        }
    }

    @Override
    public List<ConversationLastMsgDTO> findUserConversations(String loggedInUserId) {
        String validUserId = SecurityUtil.getCurrentUserIdFromSecurityContext();
        logger.info("Fetching chat list for loggedInUserId={}", validUserId);
        // String validUserId = InputSecurityUtils.secureId(loggedInUserId);
        List<ConversationLastMsgDTO> chatList = conversationLastMessageRepository
                .findUserConversations(Long.parseLong(validUserId));

        // Fetch latest active profile picture mediaId for each user in chat list
        for (ConversationLastMsgDTO dto : chatList) {
            List<Media> mediaList = mediaRepository.findByOwnerUserId(Long.parseLong(dto.getUserId()));
            if (!mediaList.isEmpty()) {
                Long mediaId = mediaList.stream()
                        .filter(media -> media.getStatus() == MediaUploadStatus.ACTIVE
                                && media.getUsageType() == MediaUsageType.PROFILE_PICTURE)
                        .sorted(Comparator.comparingLong(Media::getId).reversed())
                        .map(Media::getId)
                        .findFirst()
                        .orElse(null);
                dto.setMediaId(mediaId != null ? String.valueOf(mediaId) : null);
            }
        }

        if(logger.isDebugEnabled()) {
            logger.debug("Fetched {} conversations for userId={}", chatList.size(), validUserId);
        }
        return chatList;
    }
}
