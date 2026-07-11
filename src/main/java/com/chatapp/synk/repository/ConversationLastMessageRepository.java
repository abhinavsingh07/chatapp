package com.chatapp.synk.repository;

import com.chatapp.synk.dto.ConversationLastMsgDTO;
import com.chatapp.synk.entity.ConversationLastMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Repository
public interface ConversationLastMessageRepository extends JpaRepository<ConversationLastMessage, Long> {
    
       @Modifying // tells Spring this query changes data
       @Transactional // ensures atomic commit/rollback
       @Query(value = "INSERT INTO conversation_last_message "
                     + "(conversation_id, message_id, sender_id, content, sent_at,updated_at) " +
                     "VALUES (:conversationId, :messageId, :senderId, :content, :sentAt,:updatedAt) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "message_id = VALUES(message_id), " +
                     "sender_id = VALUES(sender_id), " +
                     "content = VALUES(content)," +
                     "sent_at = VALUES(sent_at)," +
                     "updated_at = VALUES(updated_at)", nativeQuery = true)
       void upsertLastMessage(@Param("conversationId") Long conversationId,
                     @Param("messageId") Long messageId,
                     @Param("senderId") Long senderId,
                     @Param("content") String content,
                     @Param("sentAt") Instant sentAt,
                     @Param("updatedAt") Instant updatedAt);

       @Query(""" 
              SELECT new com.chatapp.synk.dto.ConversationLastMsgDTO(
                         clm.messageId,
                         clm.conversationId,
                         clm.content,
                         clm.sentAt,
                         clm.senderId,
                         conv.conversationType,
                         u.id,
                         u.name,
                         u.profilePictureUrl
                     )
                     FROM ConversationParticipant cp_self
                     JOIN ConversationLastMessage clm
                         ON cp_self.conversationId = clm.conversationId
                     JOIN ConversationParticipant cp_other
                         ON clm.conversationId = cp_other.conversationId
                     JOIN Conversation conv
                         ON clm.conversationId = conv.id
                     JOIN User u
                         ON cp_other.userId = u.id
                     WHERE cp_self.userId = :loggedInUserId
                       AND cp_other.userId != :loggedInUserId
                     """)
       List<ConversationLastMsgDTO> findUserConversations(@Param("loggedInUserId") Long loggedInUserId);

}
