package com.chatapp.synk.util;

import com.chatapp.synk.dto.*;
import com.chatapp.synk.entity.*;
import org.springframework.security.crypto.password.PasswordEncoder;

public class Mapper {
    public static UserDTO mapToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(String.valueOf(user.getId()));
        dto.setIdentifierId(user.getIdentifierId());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setPassword(user.getPassword());
        dto.setName(user.getName());
        dto.setProfilePictureUrl(user.getProfilePictureUrl());
        dto.setAbout(user.getAbout());
        dto.setEmail(user.getEmail());
        dto.setStatus(user.getStatus());
        dto.setRoleName(user.getUserRole());
        dto.setUserlastSeen(user.getUserlastSeen());
        return dto;
    }

    public static User mapToUserEntity(UserDTO dto, PasswordEncoder passwordEncoder) {
        String generatedId = RandomUUIDGenerater.getId(User.ALIAS_USER).toString();
        User user = new User();
        user.setIdentifierId(generatedId);
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setName(dto.getName());
        user.setProfilePictureUrl(dto.getProfilePictureUrl());
        user.setAbout(dto.getAbout());
        user.setEmail(dto.getEmail());
        // user.setStatus(UserStatus.ACTIVE); Inserting from entity lifecycle hook
        // user.setUserRole(RoleName.ROLE_USER);//inserting in servicelayer
        return user;
    }

    public static Contact mapToContactEntity(ContactDTO dto) {
        String generatedId = RandomUUIDGenerater.getId(Contact.ALIAS_CONTACT).toString();
        Contact contact = new Contact();
        contact.setIdentifierId(generatedId);
        contact.setUserId(Long.parseLong(dto.getUserId()));
        contact.setContactUserId(Long.parseLong(dto.getContactUserId()));
        contact.setContactStatus(dto.getContactStatus());
        contact.setEmailStatus(dto.getEmailStatus());
        contact.setEmail(dto.getEmail());
        return contact;
    }

    public static ContactDTO mapToContactDTO(Contact contact) {
        ContactDTO dto = new ContactDTO();
        dto.setId(String.valueOf(contact.getId()));
        dto.setIdentifierId(contact.getIdentifierId());
        dto.setUserId(String.valueOf(contact.getUserId()));
        dto.setContactUserId(String.valueOf(contact.getContactUserId()));
        dto.setContactStatus(contact.getContactStatus());
        dto.setEmailStatus(contact.getEmailStatus());
        dto.setEmail(contact.getEmail());
        return dto;
    }

    public static Conversation mapToConversationEntity(ConversationDTO dto) {
        Conversation conversation = new Conversation();
        conversation.setIdentifierId(RandomUUIDGenerater.getId(Conversation.ALIAS_CONVERSATION).toString());
        conversation.setConversationType(dto.getConversationType());
        return conversation;
    }

    public static ConversationDTO mapToConversationDTO(Conversation entity) {
        ConversationDTO dto = new ConversationDTO();
        dto.setId(String.valueOf(entity.getId()));
        dto.setIdentifierId(entity.getIdentifierId());
        dto.setConversationType(entity.getConversationType());
        return dto;
    }

    public static ConversationParticipant mapToParticipantEntity(ConversationParticipantDTO dto) {
        ConversationParticipant participant = new ConversationParticipant();
        participant.setIdentifierId(RandomUUIDGenerater.getId(ConversationParticipant.ALIAS_PARTICIPANT).toString());
        participant.setConversationId(Long.parseLong(dto.getConversationId()));
        participant.setUserId(Long.parseLong(dto.getUserId()));
        return participant;
    }

    public static ConversationParticipantDTO mapToParticipantDTO(ConversationParticipant entity) {
        ConversationParticipantDTO dto = new ConversationParticipantDTO();
        dto.setId(String.valueOf(entity.getId()));
        dto.setIdentifierId(entity.getIdentifierId());
        dto.setConversationId(String.valueOf(entity.getConversationId()));
        dto.setUserId(String.valueOf(entity.getUserId()));
        return dto;
    }

    public static MessageDTO mapToMessageDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(String.valueOf(message.getId()));
        dto.setIdentifierId(message.getIdentifierId());
        dto.setConversationId(String.valueOf(message.getConversationId()));
        dto.setSenderId(String.valueOf(message.getSenderId()));
        dto.setReceiverId(String.valueOf(message.getReceiverId()));
        dto.setContent(message.getContent());
        dto.setMediaId(String.valueOf(message.getMediaId()));
        dto.setMessageStatus(message.getMessageStatus());
        dto.setSentAt(message.getSentAt().toString());
        return dto;
    }

    public static Message mapToMessageEntity(MessageDTO dto) {
        Message message = new Message();
        message.setIdentifierId(RandomUUIDGenerater.getId(Message.ALIAS_MESSAGE).toString());
        message.setConversationId(Long.parseLong(dto.getConversationId()));
        message.setSenderId(Long.parseLong(dto.getSenderId()));
        message.setReceiverId(Long.parseLong(dto.getReceiverId()));
        message.setContent(dto.getContent());
        message.setMediaId(Long.parseLong(dto.getMediaId()));
        message.setMessageStatus(dto.getMessageStatus());
        return message;
    }

    public static RefreshToken mapToRefreshTokenEntity(RefreshTokenDto dto) {
        RefreshToken refreshToken = new RefreshToken();
        String identifierId = dto.getIdentifierId() != null ? dto.getIdentifierId()
                : RandomUUIDGenerater.getId(RefreshToken.ALIAS_REFRESH_TOKEN).toString();
        refreshToken.setIdentifierId(identifierId);
        refreshToken.setUserId(Long.parseLong(dto.getUserId()));
        refreshToken.setTokenHash(dto.getTokenHash());
        refreshToken.setIssuedAt(dto.getIssuedAt());
        refreshToken.setExpiresAt(dto.getExpiresAt());
        refreshToken.setRevoked(dto.isRevoked());
        refreshToken.setRevokedAt(dto.getRevokedAt());
        refreshToken.setRevokeReason(dto.getRevokeReason());
        refreshToken.setDeviceId(dto.getDeviceId());
        refreshToken.setUserAgent(dto.getUserAgent());
        refreshToken.setIpAddress(dto.getIpAddress());
        return refreshToken;
    }

    public static RefreshTokenDto mapToRefreshTokenDto(RefreshToken refreshToken) {
        RefreshTokenDto dto = new RefreshTokenDto();
        dto.setId(String.valueOf(refreshToken.getId()));
        dto.setIdentifierId(refreshToken.getIdentifierId());
        dto.setUserId(String.valueOf(refreshToken.getUserId()));
        dto.setTokenHash(refreshToken.getTokenHash());
        dto.setIssuedAt(refreshToken.getIssuedAt());
        dto.setExpiresAt(refreshToken.getExpiresAt());
        dto.setRevoked(refreshToken.isRevoked());
        dto.setRevokedAt(refreshToken.getRevokedAt());
        dto.setRevokeReason(refreshToken.getRevokeReason());
        dto.setDeviceId(refreshToken.getDeviceId());
        dto.setUserAgent(refreshToken.getUserAgent());
        dto.setIpAddress(refreshToken.getIpAddress());
        return dto;
    }
}
