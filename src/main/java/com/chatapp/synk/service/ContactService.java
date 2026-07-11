package com.chatapp.synk.service;

import com.chatapp.synk.dto.ContactDTO;
import com.chatapp.synk.dto.ContactUserDTO;

import java.util.List;

public interface ContactService {
    ContactDTO addContact(ContactDTO contactDTO);

    List<ContactUserDTO> getContactsByUserId(String userId, String userDetailsRequired, String mediaDetailsRequired);

    void deleteContact(String contactId);
}
