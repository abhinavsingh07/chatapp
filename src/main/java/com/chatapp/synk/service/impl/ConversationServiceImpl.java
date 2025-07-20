package com.chatapp.synk.service.impl;

import com.chatapp.synk.dto.ConversationDTO;
import com.chatapp.synk.entity.Conversation;
import com.chatapp.synk.repository.ConversationRepository;
import com.chatapp.synk.service.ConversationService;
import com.chatapp.synk.util.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ConversationServiceImpl implements ConversationService {
    private static final Logger logger = LoggerFactory.getLogger(ConversationServiceImpl.class);

    @Autowired
    private ConversationRepository repository;

    @Override
    @CachePut(value = "conversationCache", key = "#result.id")
    public ConversationDTO createConversation(ConversationDTO dto) {
        logger.info("Creating new conversation");
        Conversation entity = Mapper.mapToConversationEntity(dto);
        Conversation saved = repository.save(entity);
        logger.info("Conversation saved with ID: {}", saved.getId());
        return Mapper.mapToConversationDTO(saved);
    }

    @Override
    @Cacheable(value = "conversationCache", key = "#id", unless = "#result == null")
    public ConversationDTO getConversationById(String id) {
        logger.info("Fetching conversation with ID: {}", id);
        Optional<ConversationDTO> result = repository.findById(id).map(Mapper::mapToConversationDTO);
        if (result.isEmpty()) {
            logger.warn("No conversation found with ID: {}", id);
            return null;
        }
        return result.get();
    }

    @Override
    @Cacheable(value = "conversationCache", key = "'allConversations'")
    public List<ConversationDTO> findAll() {
        logger.info("fetching all conversationIds");
        return repository.findAll().stream().map(Mapper::mapToConversationDTO).collect(Collectors.toList());
    }
}
