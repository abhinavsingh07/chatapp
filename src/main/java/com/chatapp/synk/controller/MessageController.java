package com.chatapp.synk.controller;

import com.chatapp.synk.dto.MessageDTO;
import com.chatapp.synk.response.SuccessResponse;
import com.chatapp.synk.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private MessageService messageService;

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<SuccessResponse<MessageDTO>> getMessagesByConversationId(@PathVariable String conversationId) {
        logger.info("Fetching messages for conversation: {}", conversationId);
        List<MessageDTO> messages = messageService.getMessagesByConversationId(conversationId);
        return ResponseEntity.ok(new SuccessResponse<>("200", "Messages fetched successfully", messages));
    }

    @GetMapping("/unread/{receiverId}")
    public ResponseEntity<SuccessResponse<MessageDTO>> getUnreadMessages(@PathVariable String receiverId) {
        logger.info("Fetching unread messages for receiver: {}", receiverId);
        List<MessageDTO> messages = messageService.getUnreadMessagesForReceiver(receiverId);
        return ResponseEntity.ok(new SuccessResponse<>("200", "Unread messages fetched", messages));
    }

    @PostMapping("/send")
    public ResponseEntity<SuccessResponse<MessageDTO>> sendMessage(@RequestBody MessageDTO messageDTO) {
        logger.info("Sending new message");
        MessageDTO saved = messageService.sendMessage(messageDTO);
        return ResponseEntity.ok(new SuccessResponse<>("200", "Message sent successfully", List.of(saved)));
    }

    @PutMapping("/read/{id}")
    public ResponseEntity<SuccessResponse<Void>> markMessageAsRead(@PathVariable String id) {
        logger.info("Marking message as read with ID: {}", id);
        messageService.markMessageAsRead(id);
        return ResponseEntity.ok(new SuccessResponse<>("200", "Message marked as read", Collections.emptyList()));
    }
}
