package com.chatapp.synk.enums;

public enum ConversationType {
    ONE_TO_ONE("One-to-one chat"),
    GROUP("Group chat");

    private final String description;

    ConversationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
