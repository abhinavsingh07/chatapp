package com.chatapp.synk.repository;

import com.chatapp.synk.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {
    // Extend when needed
}