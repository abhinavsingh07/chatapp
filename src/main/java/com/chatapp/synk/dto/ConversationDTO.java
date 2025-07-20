package com.chatapp.synk.dto;

import jakarta.validation.constraints.NotNull;

public class ConversationDTO {
    private String id;

    @NotNull(message = "Conversation type is required")
    private String conversationType;

    public ConversationDTO() {
    }

    public ConversationDTO(String id, String conversationType) {
        this.id = id;
        this.conversationType = conversationType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConversationType() {
        return conversationType;
    }

    public void setConversationType(String conversationType) {
        this.conversationType = conversationType;
    }
}
