package com.chatapp.synk.controller;

import com.chatapp.synk.dto.ConversationDTO;
import com.chatapp.synk.dto.ConversationLastMsgDTO;
import com.chatapp.synk.response.SuccessResponse;
import com.chatapp.synk.security.SecurityUtil;
import com.chatapp.synk.service.ConversationLastMessageService;
import com.chatapp.synk.service.ConversationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {
    private static final Logger logger = LoggerFactory.getLogger(ConversationController.class);
    private final ConversationService conversationService;

    private final ConversationLastMessageService conversationLastMessageService;

    public ConversationController(ConversationService conversationService,
            ConversationLastMessageService conversationLastMessageService) {
        this.conversationService = conversationService;
        this.conversationLastMessageService = conversationLastMessageService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<ConversationDTO>> getById(@PathVariable String id) {
        logger.debug("Fetching conversation with ID: {}", id);
        ConversationDTO convo = conversationService.getConversationById(id);

        if (convo != null) {
            logger.info("Conversation found with ID: {}", id);
            return ResponseEntity.ok(new SuccessResponse<>(HttpStatus.OK, "Conversation found", List.of(convo)));
        } else {
            logger.warn("Conversation not found with ID: {}", id);
            return ResponseEntity
                    .ok(new SuccessResponse<>(HttpStatus.NOT_FOUND, "Conversation not found", Collections.emptyList()));
        }
    }

    @PostMapping("/get-or-create/{toUserId}")
    public ResponseEntity<SuccessResponse<String>> getOrCreateConversation(@PathVariable String toUserId) {
        logger.info("Request to get or create conversation between {} and {}",
                SecurityUtil.getCurrentUserIdFromSecurityContext(), toUserId);
        // logged in user id for security check
        String loggedInUserId = SecurityUtil.getCurrentUserIdFromSecurityContext();
        String conversationId = conversationService.getOrCreateConversation(loggedInUserId, toUserId);

        if (conversationId == null) {
            logger.error("Failed to create or fetch conversation between {} and {}",
                    loggedInUserId, toUserId);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SuccessResponse<>(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Failed to create or fetch conversation", null));
        }

        logger.info("Conversation {} found/created successfully between {} and {}", conversationId, loggedInUserId,
                toUserId);
        return ResponseEntity.ok(new SuccessResponse<>(HttpStatus.OK, "Conversation found/created successfully",
                List.of(conversationId)));
    }

    @GetMapping("/last-message")
    public ResponseEntity<SuccessResponse<ConversationLastMsgDTO>> getUserConversationsLastMessage() {
        logger.debug("Fetching last message chat list");
         // logged in user id for security check
        String loggedInUserId = SecurityUtil.getCurrentUserIdFromSecurityContext();

        List<ConversationLastMsgDTO> chatList = conversationLastMessageService.findUserConversations(loggedInUserId);

        if (chatList.isEmpty()) {
            logger.warn("No conversations found for userId={}", loggedInUserId);
        } else {
            logger.info("Retrieved {} conversations for userId={}", chatList.size(), loggedInUserId);
        }

        String msg = chatList.isEmpty() ? "No conversations available" : "Conversations retrieved successfully";
        HttpStatus code = chatList.isEmpty() ? HttpStatus.NOT_FOUND : HttpStatus.OK;

        return ResponseEntity.ok(new SuccessResponse<>(code, msg, chatList));
    }

    @PostMapping
    public ResponseEntity<SuccessResponse<ConversationDTO>> create(@Valid @RequestBody ConversationDTO dto) {
        ConversationDTO created = conversationService.createConversation(dto);
        logger.info("Conversation created successfully with ID: {}", created.getId());
        return ResponseEntity.ok(new SuccessResponse<>(HttpStatus.CREATED, "Conversation created", List.of(created)));
    }

}
