package com.chatapp.synk.util;

import com.chatapp.synk.dto.*;
import com.chatapp.synk.entity.*;
import org.springframework.security.crypto.password.PasswordEncoder;

public class Mapper {
    public static UserDTO mapToUserDTO(User user) {
        if (user == null) {
            return null;
        }
        UserDTO dto = new UserDTO();
        if (user.getId() != 0) {
            dto.setId(String.valueOf(user.getId()));
        }
        if (user.getIdentifierId() != null) {
            dto.setIdentifierId(user.getIdentifierId());
        }
        if (user.getPhoneNumber() != null) {
            dto.setPhoneNumber(user.getPhoneNumber());
        }
        if (user.getPassword() != null) {
            dto.setPassword(user.getPassword());
        }
        if (user.getName() != null) {
            dto.setName(user.getName());
        }
        if (user.getProfilePictureUrl() != null) {
            dto.setProfilePictureUrl(user.getProfilePictureUrl());
        }
        if (user.getAbout() != null) {
            dto.setAbout(user.getAbout());
        }
        if (user.getEmail() != null) {
            dto.setEmail(user.getEmail());
        }
        if (user.getStatus() != null) {
            dto.setStatus(user.getStatus());
        }
        if (user.getUserRole() != null) {
            dto.setRoleName(user.getUserRole());
        }
        if (user.getUserlastSeen() != null) {
            dto.setUserlastSeen(user.getUserlastSeen());
        }
        return dto;
    }

    public static User mapToUserEntity(UserDTO dto, PasswordEncoder passwordEncoder) {
        if (dto == null || passwordEncoder == null) {
            return null;
        }
        String generatedId = RandomUUIDGenerater.getId(User.ALIAS_USER).toString();
        User user = new User();
        user.setIdentifierId(generatedId);
        if (dto.getPhoneNumber() != null) {
            user.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        if (dto.getName() != null) {
            user.setName(dto.getName());
        }
        if (dto.getProfilePictureUrl() != null) {
            user.setProfilePictureUrl(dto.getProfilePictureUrl());
        }
        if (dto.getAbout() != null) {
            user.setAbout(dto.getAbout());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        // user.setStatus(UserStatus.ACTIVE); Inserting from entity lifecycle hook
        // user.setUserRole(RoleName.ROLE_USER);//inserting in servicelayer
        return user;
    }

    public static Contact mapToContactEntity(ContactDTO dto) {
        if (dto == null) {
            return null;
        }
        String generatedId = RandomUUIDGenerater.getId(Contact.ALIAS_CONTACT).toString();
        Contact contact = new Contact();
        contact.setIdentifierId(generatedId);
        if (dto.getUserId() != null) {
            contact.setUserId(Long.parseLong(dto.getUserId()));
        }
        //contact.setContactUserId(Long.parseLong(dto.getContactUserId()));
        if (dto.getContactStatus() != null) {
            contact.setContactStatus(dto.getContactStatus());
        }
        if (dto.getEmailStatus() != null) {
            contact.setEmailStatus(dto.getEmailStatus());
        }
        if (dto.getEmail() != null) {
            contact.setEmail(dto.getEmail());
        }
        return contact;
    }

    public static ContactDTO mapToContactDTO(Contact contact) {
        if (contact == null) {
            return null;
        }
        ContactDTO dto = new ContactDTO();
        if (contact.getId() != 0) {
            dto.setId(String.valueOf(contact.getId()));
        }
        if (contact.getIdentifierId() != null) {
            dto.setIdentifierId(contact.getIdentifierId());
        }
        if (contact.getUserId() != 0) {
            dto.setUserId(String.valueOf(contact.getUserId()));
        }
        if (contact.getContactUserId() != 0) {
            dto.setContactUserId(String.valueOf(contact.getContactUserId()));
        }
        if (contact.getContactStatus() != null) {
            dto.setContactStatus(contact.getContactStatus());
        }
        if (contact.getEmailStatus() != null) {
            dto.setEmailStatus(contact.getEmailStatus());
        }
        if (contact.getEmail() != null) {
            dto.setEmail(contact.getEmail());
        }
        return dto;
    }

    public static Conversation mapToConversationEntity(ConversationDTO dto) {
        if (dto == null) {
            return null;
        }
        Conversation conversation = new Conversation();
        conversation.setIdentifierId(RandomUUIDGenerater.getId(Conversation.ALIAS_CONVERSATION).toString());
        if (dto.getConversationType() != null) {
            conversation.setConversationType(dto.getConversationType());
        }
        return conversation;
    }

    public static ConversationDTO mapToConversationDTO(Conversation entity) {
        if (entity == null) {
            return null;
        }
        ConversationDTO dto = new ConversationDTO();
        if (entity.getId() != 0) {
            dto.setId(String.valueOf(entity.getId()));
        }
        if (entity.getIdentifierId() != null) {
            dto.setIdentifierId(entity.getIdentifierId());
        }
        if (entity.getConversationType() != null) {
            dto.setConversationType(entity.getConversationType());
        }
        return dto;
    }

    public static ConversationParticipant mapToParticipantEntity(ConversationParticipantDTO dto) {
        if (dto == null) {
            return null;
        }
        ConversationParticipant participant = new ConversationParticipant();
        participant.setIdentifierId(RandomUUIDGenerater.getId(ConversationParticipant.ALIAS_PARTICIPANT).toString());
        if (dto.getConversationId() != null) {
            participant.setConversationId(Long.parseLong(dto.getConversationId()));
        }
        if (dto.getUserId() != null) {
            participant.setUserId(Long.parseLong(dto.getUserId()));
        }
        return participant;
    }

    public static ConversationParticipantDTO mapToParticipantDTO(ConversationParticipant entity) {
        if (entity == null) {
            return null;
        }
        ConversationParticipantDTO dto = new ConversationParticipantDTO();
        if (entity.getId() != 0) {
            dto.setId(String.valueOf(entity.getId()));
        }
        if (entity.getIdentifierId() != null) {
            dto.setIdentifierId(entity.getIdentifierId());
        }
        if (entity.getConversationId() != 0) {
            dto.setConversationId(String.valueOf(entity.getConversationId()));
        }
        if (entity.getUserId() != 0) {
            dto.setUserId(String.valueOf(entity.getUserId()));
        }
        return dto;
    }

    public static MessageDTO mapToMessageDTO(Message message) {
        if (message == null) {
            return null;
        }
        MessageDTO dto = new MessageDTO();
        if (message.getId() != 0) {
            dto.setId(String.valueOf(message.getId()));
        }
        if (message.getIdentifierId() != null) {
            dto.setIdentifierId(message.getIdentifierId());
        }
        if (message.getConversationId() != 0) {
            dto.setConversationId(String.valueOf(message.getConversationId()));
        }
        if (message.getSenderId() != 0) {
            dto.setSenderId(String.valueOf(message.getSenderId()));
        }
        if (message.getReceiverId() != 0) {
            dto.setReceiverId(String.valueOf(message.getReceiverId()));
        }
        if (message.getContent() != null) {
            dto.setContent(message.getContent());
        }
        if (message.getMediaId() != null) {
            dto.setMediaId(String.valueOf(message.getMediaId()));
        }
        if (message.getMessageStatus() != null) {
            dto.setMessageStatus(message.getMessageStatus());
        }
        if (message.getSentAt() != null) {
            dto.setSentAt(message.getSentAt().toString());
        }
        return dto;
    }

    public static Message mapToMessageEntity(MessageDTO dto) {
        if (dto == null) {
            return null;
        }
        Message message = new Message();
        message.setIdentifierId(RandomUUIDGenerater.getId(Message.ALIAS_MESSAGE).toString());
        if (dto.getConversationId() != null) {
            message.setConversationId(Long.parseLong(dto.getConversationId()));
        }
        if (dto.getSenderId() != null) {
            message.setSenderId(Long.parseLong(dto.getSenderId()));
        }
        if (dto.getReceiverId() != null) {
            message.setReceiverId(Long.parseLong(dto.getReceiverId()));
        }
        if (dto.getContent() != null) {
            message.setContent(dto.getContent());
        }
        if (dto.getMediaId() != null) {
            message.setMediaId(Long.parseLong(dto.getMediaId()));
        }
        if (dto.getMessageStatus() != null) {
            message.setMessageStatus(dto.getMessageStatus());
        }
        return message;
    }

    public static RefreshToken mapToRefreshTokenEntity(RefreshTokenDto dto) {
        if (dto == null) {
            return null;
        }
        RefreshToken refreshToken = new RefreshToken();
        String identifierId = dto.getIdentifierId() != null ? dto.getIdentifierId()
                : RandomUUIDGenerater.getId(RefreshToken.ALIAS_REFRESH_TOKEN).toString();
        refreshToken.setIdentifierId(identifierId);
        if (dto.getUserId() != null) {
            refreshToken.setUserId(Long.parseLong(dto.getUserId()));
        }
        if (dto.getTokenHash() != null) {
            refreshToken.setTokenHash(dto.getTokenHash());
        }
        if (dto.getIssuedAt() != null) {
            refreshToken.setIssuedAt(dto.getIssuedAt());
        }
        if (dto.getExpiresAt() != null) {
            refreshToken.setExpiresAt(dto.getExpiresAt());
        }
        refreshToken.setRevoked(dto.isRevoked());
        if (dto.getRevokedAt() != null) {
            refreshToken.setRevokedAt(dto.getRevokedAt());
        }
        if (dto.getRevokeReason() != null) {
            refreshToken.setRevokeReason(dto.getRevokeReason());
        }
        if (dto.getDeviceId() != null) {
            refreshToken.setDeviceId(dto.getDeviceId());
        }
        if (dto.getUserAgent() != null) {
            refreshToken.setUserAgent(dto.getUserAgent());
        }
        if (dto.getIpAddress() != null) {
            refreshToken.setIpAddress(dto.getIpAddress());
        }
        return refreshToken;
    }

    public static RefreshTokenDto mapToRefreshTokenDto(RefreshToken refreshToken) {
        if (refreshToken == null) {
            return null;
        }
        RefreshTokenDto dto = new RefreshTokenDto();
        if (refreshToken.getId() != 0) {
            dto.setId(String.valueOf(refreshToken.getId()));
        }
        if (refreshToken.getIdentifierId() != null) {
            dto.setIdentifierId(refreshToken.getIdentifierId());
        }
        if (refreshToken.getUserId() != 0) {
            dto.setUserId(String.valueOf(refreshToken.getUserId()));
        }
        if (refreshToken.getTokenHash() != null) {
            dto.setTokenHash(refreshToken.getTokenHash());
        }
        if (refreshToken.getIssuedAt() != null) {
            dto.setIssuedAt(refreshToken.getIssuedAt());
        }
        if (refreshToken.getExpiresAt() != null) {
            dto.setExpiresAt(refreshToken.getExpiresAt());
        }
        dto.setRevoked(refreshToken.isRevoked());
        if (refreshToken.getRevokedAt() != null) {
            dto.setRevokedAt(refreshToken.getRevokedAt());
        }
        if (refreshToken.getRevokeReason() != null) {
            dto.setRevokeReason(refreshToken.getRevokeReason());
        }
        if (refreshToken.getDeviceId() != null) {
            dto.setDeviceId(refreshToken.getDeviceId());
        }
        if (refreshToken.getUserAgent() != null) {
            dto.setUserAgent(refreshToken.getUserAgent());
        }
        if (refreshToken.getIpAddress() != null) {
            dto.setIpAddress(refreshToken.getIpAddress());
        }
        return dto;
    }
}
