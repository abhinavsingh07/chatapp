package com.chatapp.synk.service;

import com.chatapp.synk.dto.ContactsDTO;

import java.util.List;

public interface ContactsService {
    ContactsDTO addContact(ContactsDTO contactsDTO);

    List<ContactsDTO> getContacts(String userId);
}
