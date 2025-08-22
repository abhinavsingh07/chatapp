package com.chatapp.synk.controller;

import com.chatapp.synk.dto.ConversationParticipantDTO;
import com.chatapp.synk.response.SuccessResponse;
import com.chatapp.synk.service.ConversationParticipantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationParticipantControllerTest {

    @Mock
    private ConversationParticipantService participantService;

    @InjectMocks
    private ConversationParticipantController controller;

    private ConversationParticipantDTO sampleDto;

    @BeforeEach
    void setUp() {
        sampleDto = new ConversationParticipantDTO();
        sampleDto.setId("p1");
        sampleDto.setConversationId("c1");
    }

    @Test
    void testAddParticipant() {
        when(participantService.addParticipant(sampleDto)).thenReturn(sampleDto);

        ResponseEntity<SuccessResponse<ConversationParticipantDTO>> response = controller.add(sampleDto);

        assertThat(response.getBody().getResponseCode()).isEqualTo("201");
        assertThat(response.getBody().getData()).contains(sampleDto);
        verify(participantService).addParticipant(sampleDto);
    }

    @Test
    void testGetById_found() {
        when(participantService.getParticipantById("p1")).thenReturn(sampleDto);

        ResponseEntity<SuccessResponse<ConversationParticipantDTO>> response = controller.getById("p1");

        assertThat(response.getBody().getResponseCode()).isEqualTo("200");
        assertThat(response.getBody().getData()).contains(sampleDto);
        verify(participantService).getParticipantById("p1");
    }

    @Test
    void testGetById_notFound() {
        when(participantService.getParticipantById("p2")).thenReturn(null);

        ResponseEntity<SuccessResponse<ConversationParticipantDTO>> response = controller.getById("p2");

        assertThat(response.getBody().getResponseCode()).isEqualTo("404");
        assertThat(response.getBody().getData()).isEmpty();
        verify(participantService).getParticipantById("p2");
    }

    @Test
    void testGetByConversation_found() {
        when(participantService.getParticipantsByConversationId("c1"))
                .thenReturn(List.of(sampleDto));

        ResponseEntity<SuccessResponse<ConversationParticipantDTO>> response = controller.getByConversation("c1");

        assertThat(response.getBody().getResponseCode()).isEqualTo("200");
        assertThat(response.getBody().getData()).contains(sampleDto);
        verify(participantService).getParticipantsByConversationId("c1");
    }

    @Test
    void testGetByConversation_notFound() {
        when(participantService.getParticipantsByConversationId("c2"))
                .thenReturn(Collections.emptyList());

        ResponseEntity<SuccessResponse<ConversationParticipantDTO>> response = controller.getByConversation("c2");

        assertThat(response.getBody().getResponseCode()).isEqualTo("404");
        assertThat(response.getBody().getData()).isEmpty();
        verify(participantService).getParticipantsByConversationId("c2");
    }

    @Test
    void testDelete() {
        doNothing().when(participantService).deleteByConversationid("p1");

        ResponseEntity<SuccessResponse<Void>> response = controller.delete("p1");

        assertThat(response.getBody().getResponseCode()).isEqualTo("200");
        verify(participantService).deleteByConversationid("p1");
    }
}
