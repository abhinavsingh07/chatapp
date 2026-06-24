package com.chatapp.synk.util;

import com.chatapp.synk.dto.*;
import com.chatapp.synk.entity.*;
import org.springframework.security.crypto.password.PasswordEncoder;

public class Mapper {
    public static UserDTO mapToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
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
        user.setId(generatedId);
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
        contact.setId(generatedId);
        contact.setUserId(dto.getUserId());
        contact.setContactUserId(dto.getContactUserId());
        contact.setContactStatus(dto.getContactStatus());
        contact.setEmailStatus(dto.getEmailStatus());
        contact.setEmail(dto.getEmail());
        return contact;
    }

    public static ContactDTO mapToContactDTO(Contact contact) {
        ContactDTO dto = new ContactDTO();
        dto.setId(contact.getId());
        dto.setUserId(contact.getUserId());
        dto.setContactUserId(contact.getContactUserId());
        dto.setContactStatus(contact.getContactStatus());
        dto.setEmailStatus(contact.getEmailStatus());
        dto.setEmail(contact.getEmail());
        return dto;
    }

    public static Conversation mapToConversationEntity(ConversationDTO dto) {
        Conversation conversation = new Conversation();
        conversation.setId(RandomUUIDGenerater.getId(Conversation.ALIAS_CONVERSATION).toString());
        conversation.setConversationType(dto.getConversationType());
        return conversation;
    }

    public static ConversationDTO mapToConversationDTO(Conversation entity) {
        return new ConversationDTO(entity.getId(), entity.getConversationType());
    }

    public static ConversationParticipant mapToParticipantEntity(ConversationParticipantDTO dto) {
        ConversationParticipant participant = new ConversationParticipant();
        participant.setId(RandomUUIDGenerater.getId(ConversationParticipant.ALIAS_PARTICIPANT).toString());
        participant.setConversationId(dto.getConversationId());
        participant.setUserId(dto.getUserId());
        return participant;
    }

    public static ConversationParticipantDTO mapToParticipantDTO(ConversationParticipant entity) {
        return new ConversationParticipantDTO(entity.getId(), entity.getConversationId(), entity.getUserId());
    }

    public static MessageDTO mapToMessageDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversationId());
        dto.setSenderId(message.getSenderId());
        dto.setReceiverId(message.getReceiverId());
        dto.setContent(message.getContent());
        dto.setMediaId(message.getMediaId());
        dto.setMessageStatus(message.getMessageStatus());
        dto.setSentAt(message.getSentAt().toString());
        return dto;
    }

    public static Message mapToMessageEntity(MessageDTO dto) {
        Message message = new Message();
        message.setId(RandomUUIDGenerater.getId(Message.ALIAS_MESSAGE).toString());
        message.setConversationId(dto.getConversationId());
        message.setSenderId(dto.getSenderId());
        message.setReceiverId(dto.getReceiverId());
        message.setContent(dto.getContent());
        message.setMediaId(dto.getMediaId());
        message.setMessageStatus(dto.getMessageStatus());
        return message;
    }

    public static RefreshToken mapToRefreshTokenEntity(RefreshTokenDto dto) {
        RefreshToken refreshToken = new RefreshToken();
        String id = dto.getId() != null ? dto.getId() : RandomUUIDGenerater.getId(RefreshToken.ALIAS_REFRESH_TOKEN).toString();
        refreshToken.setId(id);
        refreshToken.setUserId(dto.getUserId());
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
        dto.setId(refreshToken.getId());
        dto.setUserId(refreshToken.getUserId());
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
    }}

