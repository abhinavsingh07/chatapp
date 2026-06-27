package com.chatapp.synk.service.impl;

import com.chatapp.synk.security_validator.InputSecurityUtils;
import com.chatapp.synk.security_validator.InputValidationAndSanitizationService;
import com.chatapp.synk.dto.ConversationParticipantDTO;
import com.chatapp.synk.entity.ConversationParticipant;
import com.chatapp.synk.repository.ConversationParticipantRepository;
import com.chatapp.synk.service.ConversationParticipantService;
import com.chatapp.synk.util.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ConversationParticipantServiceImpl implements ConversationParticipantService {

    private static final Logger logger = LoggerFactory.getLogger(ConversationParticipantServiceImpl.class);

    private final ConversationParticipantRepository repository;

    public ConversationParticipantServiceImpl(ConversationParticipantRepository repository) {
        this.repository = repository;
    }

    @Override
    @CachePut(value = "participantCache", key = "#result.id")
    public ConversationParticipantDTO addParticipant(ConversationParticipantDTO dto) {
        if (logger.isDebugEnabled()) {
            logger.debug("Adding participant to conversation {}", dto.getConversationId());
        }
        ConversationParticipantDTO validDTO = InputValidationAndSanitizationService.validateAndSanitize(dto);
        ConversationParticipant entity = Mapper.mapToParticipantEntity(validDTO);
        ConversationParticipant saved = repository.save(entity);

        logger.info("Participant added with ID: {}", saved.getId());
        return Mapper.mapToParticipantDTO(saved);
    }

    @Override
    @Cacheable(value = "participantCache", key = "#id", unless = "#result == null")
    public ConversationParticipantDTO getParticipantById(String id) {
        if (logger.isDebugEnabled()) {
            logger.debug("Fetching participant by ID: {}", id);
        }
        String validId = InputSecurityUtils.secureId(id);
        Optional<ConversationParticipantDTO> result = repository
                .findById(Long.parseLong(validId))
                .map(Mapper::mapToParticipantDTO);

        if (result.isEmpty()) {
            logger.warn("No conversation participant found with ID: {}", validId);
            return null;
        }
        return result.get();
    }

    @Override
    @Cacheable(value = "participantListCache", key = "#conversationId", unless = "#result == null or #result.isEmpty()")
    public List<ConversationParticipantDTO> getParticipantsByConversationId(String conversationId) {
        if (logger.isDebugEnabled()) {
            logger.debug("Fetching participants for conversation ID: {}", conversationId.trim());
        }
        String validId = InputSecurityUtils.secureId(conversationId);
        List<ConversationParticipant> list = repository.findByConversationId(Long.parseLong(validId));
        return list.stream().map(Mapper::mapToParticipantDTO).collect(Collectors.toList());
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "participantCache", key = "#id", beforeInvocation = true),
            @CacheEvict(value = "participantListCache", key = "#id", beforeInvocation = true)
    })
    public void deleteByConversationid(String id) {
        if (logger.isDebugEnabled()) {
            logger.debug("Removing all conversation participants with conversation ID: {}", id);
        }
        String validId = InputSecurityUtils.secureId(id);
        List<ConversationParticipant> list = repository.findByConversationId(Long.parseLong(validId));
        for (ConversationParticipant cp : list) {
            repository.deleteById(cp.getId());
        }
    }
}
