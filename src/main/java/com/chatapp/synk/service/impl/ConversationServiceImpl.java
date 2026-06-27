package com.chatapp.synk.service.impl;

import com.chatapp.synk.security_validator.InputSecurityUtils;
import com.chatapp.synk.security_validator.InputValidationAndSanitizationService;
import com.chatapp.synk.dto.ConversationDTO;
import com.chatapp.synk.entity.Conversation;
import com.chatapp.synk.entity.ConversationParticipant;
import com.chatapp.synk.enums.ConversationType;
import com.chatapp.synk.exceptionHandler.ServiceException;
import com.chatapp.synk.repository.ConversationParticipantRepository;
import com.chatapp.synk.repository.ConversationRepository;
import com.chatapp.synk.security.SecurityUtil;
import com.chatapp.synk.service.ConversationService;
import com.chatapp.synk.util.Mapper;
import com.chatapp.synk.util.RandomUUIDGenerater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ConversationServiceImpl implements ConversationService {
    private static final Logger logger = LoggerFactory.getLogger(ConversationServiceImpl.class);

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;

    public ConversationServiceImpl(ConversationRepository conversationRepository,
            ConversationParticipantRepository participantRepository) {
        this.conversationRepository = conversationRepository;
        this.participantRepository = participantRepository;
    }

    @Override
    @Caching(put = { @CachePut(value = "conversationCache", key = "#result.id", unless = "#result == null") }, evict = {
            @CacheEvict(value = "conversationCache", key = "'allConversations'", beforeInvocation = true) })
    public ConversationDTO createConversation(ConversationDTO dto) {
        ConversationDTO validTO = InputValidationAndSanitizationService.validateAndSanitize(dto);
        Conversation entity = Mapper.mapToConversationEntity(validTO);
        Conversation saved = conversationRepository.save(entity);

        logger.info("Conversation created with ID: {}", saved.getId());
        return Mapper.mapToConversationDTO(saved);
    }

    @Override
    @Cacheable(value = "conversationCache", key = "#id", unless = "#result == null")
    public ConversationDTO getConversationById(String id) {
        String validId = InputSecurityUtils.secureId(id);
        Optional<ConversationDTO> result = conversationRepository.findById(Long.parseLong(validId))
                .map(Mapper::mapToConversationDTO);

        if (result.isEmpty()) {
            logger.warn("No conversation found with ID: {}", id);
            return null;
        }
        // Debug only when found (not spammy at scale)
        if (logger.isDebugEnabled()) {
            logger.debug("Fetched conversation with ID: {}", id);
        }
        return result.get();
    }

    @Override
    @Cacheable(value = "conversationListCache", key = "'allConversations'")
    public List<ConversationDTO> findAll() {
        if (logger.isDebugEnabled()) {
            logger.debug("Fetching all conversations from DB");
        }
        return conversationRepository.findAll().stream().map(Mapper::mapToConversationDTO).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "conversationIdLookupCache", key = "T(com.chatapp.synk.security.SecurityUtil).getCurrentUserIdFromSecurityContext() + '_' + #contactUserId", unless = "#result == null")
    @Transactional
    public String getOrCreateConversation(String loggedInUserId, String contactUserId) {

        String loggedInUserValidId = SecurityUtil.getCurrentUserIdFromSecurityContext();
        String contactUserValidId = InputSecurityUtils.secureId(contactUserId);

        if (logger.isDebugEnabled()) {
            logger.debug("Get or create conversation request between [{}] and [{}]", loggedInUserValidId,
                    contactUserValidId);
        }

        if (loggedInUserValidId.equals(contactUserValidId)) {
            throw new ServiceException("Cannot create conversation with yourself");
        }

        // This is concurrency control mechanism by introducing PrivateChatKey
        // column with unique index. This ensures that only one conversation
        // exists between two users, even if multiple threads attempt to create it
        // simultaneously.
        String key = buildPrivateChatKey(loggedInUserValidId, contactUserValidId);

        // Fast path: single indexed column lookup — no joins
        Optional<Conversation> existing = conversationRepository.findByPrivateChatKey(key);
        if (existing.isPresent()) {
            logger.info("Existing conversation [{}] reused for key [{}]", existing.get().getId(), key);
            return String.valueOf(existing.get().getId());
        }

        // Try to insert — DB unique constraint on private_chat_key guarantees only one
        // winner
        try {
            String newConversationIdentifierId = RandomUUIDGenerater.getId(Conversation.ALIAS_CONVERSATION).toString();
            // save conversation db call
            Conversation conversationEntityCreated = conversationRepository
                    .save(new Conversation(newConversationIdentifierId, ConversationType.ONE_TO_ONE.toString(), key));

            String participantIdentifierId1 = RandomUUIDGenerater.getId(ConversationParticipant.ALIAS_PARTICIPANT)
                    .toString();
            String participantIdentifierId2 = RandomUUIDGenerater.getId(ConversationParticipant.ALIAS_PARTICIPANT)
                    .toString();
            //save convsersation participants db call
            participantRepository.saveAll(List.of(
                    new ConversationParticipant(
                            participantIdentifierId1, conversationEntityCreated.getId(),
                            Long.parseLong(loggedInUserValidId)),
                    new ConversationParticipant(
                            participantIdentifierId2,
                            conversationEntityCreated.getId(), Long.parseLong(contactUserValidId))));

            logger.info("New conversation [{}] created for key [{}]", newConversationIdentifierId, key);
            return newConversationIdentifierId;

        } catch (DataIntegrityViolationException e) {
            // Another thread won the race and already inserted — read its result
            logger.info("Race condition on key [{}] — reading winner's conversation", key);
            return conversationRepository.findByPrivateChatKey(key)
                    .map(conversation -> String.valueOf(conversation.getId()))
                    .orElseThrow(() -> new ServiceException("Could not create or retrieve conversation"));
        }
    }

    private static String buildPrivateChatKey(String userId1, String userId2) {
        return userId1.compareTo(userId2) <= 0
                ? userId1 + ":" + userId2
                : userId2 + ":" + userId1;
    }
}
