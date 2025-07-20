package com.chatapp.synk.controller;

import com.chatapp.synk.dto.ConversationDTO;
import com.chatapp.synk.enums.ConversationType;
import com.chatapp.synk.security.JwtAuthFilter;
import com.chatapp.synk.service.ConversationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ConversationController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class)
})
@AutoConfigureMockMvc(addFilters = false)
@Import(ConversationControllerTest.TestConfig.class)
public class ConversationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConversationService conversationService;

    private ConversationDTO sampleConversation;

    @BeforeEach
    public void setup() {
        Mockito.reset(conversationService);
        sampleConversation = new ConversationDTO("123_CONVO", ConversationType.ONE_TO_ONE.toString());
    }

    @Test
    public void testGetConversationById_found() throws Exception {
        when(conversationService.getConversationById("123_CONVO")).thenReturn(sampleConversation);

        mockMvc.perform(get("/api/conversations/123_CONVO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("123_CONVO"));
    }

    @Test
    public void testGetConversationById_notFound() throws Exception {
        when(conversationService.getConversationById("123_CONVO")).thenReturn(null);

        mockMvc.perform(get("/api/conversations/123_CONVO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode").value("404"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    public void testGetAllConversations() throws Exception {
        List<ConversationDTO> conversations = List.of(sampleConversation);
        when(conversationService.findAll()).thenReturn(conversations);
        mockMvc.perform(get("/api/conversations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("123_CONVO"));
    }

    @TestConfiguration
    static class TestConfig {

        @Bean(name = "conversationService")
        @Primary
        public ConversationService conversationService() {
            return mock(ConversationService.class);
        }
    }
}
