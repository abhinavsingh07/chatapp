package com.chatapp.synk.service;

import com.chatapp.synk.dto.ContactDTO;
import com.chatapp.synk.dto.UserDTO;

import java.util.List;

public interface ContactService {
    ContactDTO addContact(ContactDTO contactDTO);

    List<UserDTO> getContactsByUserId(String userId);

    void deleteContact(String contactId);
}
