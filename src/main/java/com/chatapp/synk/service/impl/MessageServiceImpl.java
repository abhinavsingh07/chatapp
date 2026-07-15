package com.chatapp.synk.service.impl;

import com.chatapp.synk.dto.MessageDTO;
import com.chatapp.synk.entity.Message;
import com.chatapp.synk.exceptionHandler.ServiceException;
import com.chatapp.synk.repository.ConversationParticipantRepository;
import com.chatapp.synk.repository.MessageRepository;
import com.chatapp.synk.security.SecurityUtil;
import com.chatapp.synk.security_validator.InputSecurityUtils;
import com.chatapp.synk.security_validator.InputValidationAndSanitizationService;
import com.chatapp.synk.mediaUpload.service.MediaUploadService;
import com.chatapp.synk.service.ConversationLastMessageService;
import com.chatapp.synk.service.MessageService;
import com.chatapp.synk.util.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);
    private final MessageRepository messageRepository;
    private final ConversationParticipantRepository participantRepository;
    private final ConversationLastMessageService conversationLastMessageService;
    private final MediaUploadService mediaUploadService;

    public MessageServiceImpl(MessageRepository messageRepository,
            ConversationParticipantRepository participantRepository,
            ConversationLastMessageService conversationLastMessageService,
            MediaUploadService mediaUploadService) {
        this.messageRepository = messageRepository;
        this.participantRepository = participantRepository;
        this.conversationLastMessageService = conversationLastMessageService;
        this.mediaUploadService = mediaUploadService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageDTO> getMessagesByConversationId(String conversationId) {
        String validConvId = InputSecurityUtils.secureId(conversationId);
        String loggedInUserId = InputSecurityUtils.secureId(SecurityUtil.getCurrentUserIdFromSecurityContext());

        // Verify the logged-in user is a participant of this conversation
        if (!participantRepository.existsByConversationIdAndUserId(
                Long.parseLong(validConvId), Long.parseLong(loggedInUserId))) {
            logger.warn("User [{}] is not a participant of conversation [{}]", loggedInUserId, validConvId);
            throw new ServiceException("Access denied: User is not a participant of this conversation");
        }

        return messageRepository.findByConversationIdOrderBySentAtAsc(Long.parseLong(validConvId))
                .stream()
                .map(Mapper::mapToMessageDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    /**
     * Atomically save a message and associate media files with it.
     * Both operations are performed in a single transaction.
     * calling from chatmessageListener to save message and update media ids in
     * single transaction
     *
     * @param messageDTO  The message to save
     * @param mediaIdsStr Semicolon-separated media IDs (e.g., "1;2;3")
     * @param fromUserId  The user ID of the message sender (media owner)
     */
    public void saveMessageWithMediaIds(MessageDTO messageDTO, String mediaIdsStr, Long fromUserId) {
        logger.info("Saving message with media associations for userId: {}", fromUserId);
        MessageDTO validMessageDTO = InputValidationAndSanitizationService.validateAndSanitize(messageDTO);
        // Save the message
        MessageDTO savedMessage = saveMessage(validMessageDTO);
        logger.debug("Message saved with ID: {}", savedMessage.getId());

        // Update media with messageId if media IDs are present
        if (mediaIdsStr != null && !mediaIdsStr.trim().isEmpty()) {
            List<Long> mediaIds = Arrays.stream(mediaIdsStr.split(";"))
                    .map(String::trim)
                    .filter(id -> !id.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());

            if (!mediaIds.isEmpty() && savedMessage != null && savedMessage.getId() != null) {
                // Batch update — single query instead of N individual calls
                mediaUploadService.updateMessageIds(fromUserId, mediaIds,
                        Long.valueOf(savedMessage.getId()));
                logger.info("Successfully associated {} media files with messageId: {}", mediaIds.size(),
                        savedMessage.getId());
            }
        }
    }

    @Override
    @Transactional
    public MessageDTO saveMessage(MessageDTO validDTO) {

        // Idempotency fast-path: client retrying the same message returns the
        // already-saved one
        if (validDTO.getClientMessageId() != null) {
            Message existing = messageRepository.findByClientMessageId(validDTO.getClientMessageId())
                    .orElse(null);
            if (existing != null) {
                logger.warn("Duplicate detected for clientMessageId [{}], returning existing [{}]",
                        validDTO.getClientMessageId(), existing.getId());
                return Mapper.mapToMessageDTO(existing);
            }
        }

        logger.info("Saving message from {} to {}", validDTO.getSenderId(), validDTO.getReceiverId());
        try {
            Message message = Mapper.mapToMessageEntity(validDTO);
            Message savedMessage = messageRepository.save(message);
            // upsert last message for the conversation
            conversationLastMessageService.upsertLastMessage(
                    String.valueOf(savedMessage.getConversationId()),
                    String.valueOf(savedMessage.getId()),
                    String.valueOf(savedMessage.getSenderId()),
                    savedMessage.getContent());
            if (logger.isDebugEnabled()) {
                logger.debug("Last message upserted for conversationId: {}", savedMessage.getConversationId());
                logger.debug("Message saved with ID: {}", savedMessage.getId());
            }

            return Mapper.mapToMessageDTO(savedMessage);

        } catch (DataIntegrityViolationException e) {
            // Race: another thread inserted the same clientMessageId just now — return its
            // result
            logger.warn("Race condition on clientMessageId [{}] — returning winner's message",
                    validDTO.getClientMessageId());
            return messageRepository.findByClientMessageId(validDTO.getClientMessageId())
                    .map(Mapper::mapToMessageDTO)
                    .orElseThrow(() -> new ServiceException("Could not save or retrieve message"));
        }
    }

    @Override
    @Transactional
    public void markMessageAsRead(String messageId) {
        String validId = InputSecurityUtils.secureId(messageId);
        Message message = messageRepository.findById(Long.parseLong(validId)).orElseThrow(() -> {
            logger.warn("Message not found with ID: {}", validId);
            return new ServiceException("Message not found", HttpStatus.NOT_FOUND);
        });
        // message.setIsRead(true);
        messageRepository.save(message);
        logger.info("Message marked as read. ID: {}", validId);
    }

    @Override
    public List<MessageDTO> getUnreadMessagesForReceiver(String conversationId, String receiverId) {
        String receiverValidId = InputSecurityUtils.secureId(receiverId);
        String conversationValidId = InputSecurityUtils.secureId(conversationId);
        return messageRepository
                .findByConversationIdAndReceiverId(Long.parseLong(conversationValidId), Long.parseLong(receiverValidId))
                .stream()
                .map(Mapper::mapToMessageDTO)
                .collect(Collectors.toList());
    }

}
