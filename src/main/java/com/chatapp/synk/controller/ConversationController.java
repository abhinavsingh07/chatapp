package com.chatapp.synk.controller;

import com.chatapp.synk.dto.ConversationDTO;
import com.chatapp.synk.response.SuccessResponse;
import com.chatapp.synk.service.ConversationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {
    private static final Logger logger = LoggerFactory.getLogger(ConversationController.class);

    @Autowired
    private ConversationService conversationService;

    @PostMapping
    public ResponseEntity<SuccessResponse<ConversationDTO>> create(@Valid @RequestBody ConversationDTO dto) {
        logger.info("Received request to create conversation");
        ConversationDTO created = conversationService.createConversation(dto);
        return ResponseEntity.ok(new SuccessResponse<>("201", "Conversation created", List.of(created)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<ConversationDTO>> getById(@PathVariable String id) {
        logger.info("Fetching conversation with ID: {}", id);
        ConversationDTO convo = conversationService.getConversationById(id);

        if (convo != null) {
            return ResponseEntity.ok(new SuccessResponse<>("200", "Conversation found", List.of(convo)));
        } else {
            logger.warn("Conversation not found with ID: {}", id);
            return ResponseEntity.ok(new SuccessResponse<>("404", "Conversation not found", Collections.emptyList()));
        }
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<ConversationDTO>> getAllConversations() {
        logger.info("Fetching all conversations");
        List<ConversationDTO> conversations = conversationService.findAll();

        if (conversations.isEmpty()) {
            logger.warn("No conversations found");
        } else {
            logger.info("Found {} conversations", conversations.size());
        }

        String msg = conversations.isEmpty() ? "No conversations available" : "Conversations retrieved";
        String code = conversations.isEmpty() ? "404" : "200";

        return ResponseEntity.ok(new SuccessResponse<>(code, msg, conversations));
    }

}
