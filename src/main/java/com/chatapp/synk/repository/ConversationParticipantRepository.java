package com.chatapp.synk.repository;

import com.chatapp.synk.entity.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, Long> {
    // Find all participants for a given conversation ID
    List<ConversationParticipant> findByConversationId(Long conversationId);

    /**
     * Efficient existence check — returns true if the user is a participant
     * of the conversation, without loading full entity data.
     */
    boolean existsByConversationIdAndUserId(Long conversationId, Long userId);
}
