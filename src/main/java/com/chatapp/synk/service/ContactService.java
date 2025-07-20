package com.chatapp.synk.service;

import com.chatapp.synk.dto.ContactDTO;

import java.util.List;

public interface ContactService {
    ContactDTO addContact(ContactDTO contactDTO);

    List<ContactDTO> getContactsByUserId(String userId);
}
