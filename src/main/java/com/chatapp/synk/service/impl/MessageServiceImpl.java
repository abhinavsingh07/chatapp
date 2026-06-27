package com.chatapp.synk.service.impl;

import com.chatapp.synk.dto.MessageDTO;
import com.chatapp.synk.entity.Message;
import com.chatapp.synk.exceptionHandler.ServiceException;
import com.chatapp.synk.repository.MessageRepository;
import com.chatapp.synk.security_validator.InputSecurityUtils;
import com.chatapp.synk.security_validator.InputValidationAndSanitizationService;
import com.chatapp.synk.service.ConversationLastMessageService;
import com.chatapp.synk.service.MessageService;
import com.chatapp.synk.util.Mapper;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);
    private final MessageRepository messageRepository;

    private final ConversationLastMessageService conversationLastMessageService;

    public MessageServiceImpl(MessageRepository messageRepository,
            ConversationLastMessageService conversationLastMessageService) {
        this.messageRepository = messageRepository;
        this.conversationLastMessageService = conversationLastMessageService;
    }

    @Override
    public List<MessageDTO> getMessagesByConversationId(String conversationId) {
        String validId = InputSecurityUtils.secureId(conversationId);
        return messageRepository.findByConversationIdOrderBySentAtAsc(Long.parseLong(validId))
                .stream()
                .map(Mapper::mapToMessageDTO)
                .collect(Collectors.toList());
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

    @Override
    @Transactional
    public MessageDTO saveMessage(MessageDTO messageDTO) {
        MessageDTO validDTO = InputValidationAndSanitizationService.validateAndSanitize(messageDTO);

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
}
