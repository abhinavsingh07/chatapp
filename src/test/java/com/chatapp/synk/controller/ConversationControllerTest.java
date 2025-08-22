package com.chatapp.synk.controller;

import com.chatapp.synk.dto.ConversationDTO;
import com.chatapp.synk.enums.ConversationType;
import com.chatapp.synk.response.SuccessResponse;
import com.chatapp.synk.service.ConversationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationControllerTest {

    @Mock
    private ConversationService conversationService;

    @InjectMocks
    private ConversationController conversationController;

    private ConversationDTO conversationDTO;

    @BeforeEach
    void setUp() {
        conversationDTO = new ConversationDTO();
        conversationDTO.setId("1");
        conversationDTO.setConversationType(ConversationType.ONE_TO_ONE.toString());
    }

    @Test
    void testCreateConversation() {
        when(conversationService.createConversation(any(ConversationDTO.class))).thenReturn(conversationDTO);

        ResponseEntity<SuccessResponse<ConversationDTO>> response = conversationController.create(conversationDTO);

        assertEquals("201", response.getBody().getResponseCode());
        assertEquals("Conversation created", response.getBody().getMessage());
        assertEquals(1, response.getBody().getData().size());
        verify(conversationService, times(1)).createConversation(any(ConversationDTO.class));
    }

    @Test
    void testGetById_Found() {
        when(conversationService.getConversationById("1")).thenReturn(conversationDTO);

        ResponseEntity<SuccessResponse<ConversationDTO>> response = conversationController.getById("1");

        assertEquals("200", response.getBody().getResponseCode());
        assertEquals("Conversation found", response.getBody().getMessage());
        assertEquals("1", response.getBody().getData().get(0).getId());
    }

    @Test
    void testGetById_NotFound() {
        when(conversationService.getConversationById("99")).thenReturn(null);

        ResponseEntity<SuccessResponse<ConversationDTO>> response = conversationController.getById("99");

        assertEquals("404", response.getBody().getResponseCode());
        assertEquals("Conversation not found", response.getBody().getMessage());
        assertTrue(response.getBody().getData().isEmpty());
    }

    @Test
    void testGetAllConversations_Found() {
        when(conversationService.findAll()).thenReturn(List.of(conversationDTO));

        ResponseEntity<SuccessResponse<ConversationDTO>> response = conversationController.getAllConversations();

        assertEquals("200", response.getBody().getResponseCode());
        assertEquals("Conversations retrieved", response.getBody().getMessage());
        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    void testGetAllConversations_NotFound() {
        when(conversationService.findAll()).thenReturn(Collections.emptyList());

        ResponseEntity<SuccessResponse<ConversationDTO>> response = conversationController.getAllConversations();

        assertEquals("404", response.getBody().getResponseCode());
        assertEquals("No conversations available", response.getBody().getMessage());
        assertTrue(response.getBody().getData().isEmpty());
    }

    @Test
    void testGetOrCreateConversation_Success() {
        when(conversationService.getOrCreateConversation("user1", "user2")).thenReturn("convo123");

        ResponseEntity<SuccessResponse<String>> response = conversationController.getOrCreateConversation("user1", "user2");

        assertEquals("200", response.getBody().getResponseCode());
        assertEquals("Conversation found/created successfully", response.getBody().getMessage());
        assertEquals("convo123", response.getBody().getData().get(0));
    }

    @Test
    void testGetOrCreateConversation_Failure() {
        when(conversationService.getOrCreateConversation("user1", "user2")).thenReturn(null);

        ResponseEntity<SuccessResponse<String>> response = conversationController.getOrCreateConversation("user1", "user2");

        assertEquals("500", response.getBody().getResponseCode());
        assertEquals("Failed to create or fetch conversation", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }
}
