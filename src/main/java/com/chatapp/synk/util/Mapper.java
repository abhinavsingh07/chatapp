package com.chatapp.synk.util;

import com.chatapp.synk.dto.ContactsDTO;
import com.chatapp.synk.dto.UsersDTO;
import com.chatapp.synk.entity.Contacts;
import com.chatapp.synk.entity.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

public class Mapper {

    private static final String ALIAS_USER = "USER";

    private static final String ALIAS_CONTACT="CONT";

    public static UsersDTO mapToUserDTO(Users users) {
        UsersDTO dto = new UsersDTO();
        dto.setId(users.getId());
        dto.setPhoneNumber(users.getPhoneNumber());
        dto.setName(users.getName());
        dto.setProfilePictureUrl(users.getProfilePictureUrl());
        dto.setAbout(users.getAbout());
        return dto;
    }

    public static Users mapToUserEntity(UsersDTO dto) {
        String generatedId = RandomUUIDGenerater.getId(ALIAS_USER).toString();
        Users user = new Users();
        user.setId(generatedId);
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setName(dto.getName());
        user.setProfilePictureUrl(dto.getProfilePictureUrl());
        user.setAbout(dto.getAbout());
        return user;
    }

    public static Contacts mapToContactEntity(ContactsDTO dto) {
        String generatedId = RandomUUIDGenerater.getId(ALIAS_CONTACT).toString();
        Contacts contact = new Contacts();
        contact.setId(generatedId);
        contact.setUserId(dto.getUserId());
        contact.setContactUserId(dto.getContactUserId());
        return contact;
    }

    public static ContactsDTO mapToContactDTO(Contacts contact) {
        ContactsDTO dto = new ContactsDTO();
        dto.setId(contact.getId());
        dto.setUserId(contact.getUserId());
        dto.setContactUserId(contact.getContactUserId());
        return dto;
    }
}
