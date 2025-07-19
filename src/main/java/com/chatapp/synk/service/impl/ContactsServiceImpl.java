package com.chatapp.synk.service.impl;

import com.chatapp.synk.dto.ContactsDTO;
import com.chatapp.synk.entity.Contacts;
import com.chatapp.synk.exceptionHandler.ServiceException;
import com.chatapp.synk.repository.ContactsRepository;
import com.chatapp.synk.service.ContactsService;
import com.chatapp.synk.util.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContactsServiceImpl implements ContactsService {

    @Autowired
    private  ContactsRepository contactRepository;
    @Override
    public ContactsDTO addContact(ContactsDTO contactsDTO) {
        if (contactRepository.findByUserIdAndContactUserId(contactsDTO.getUserId(), contactsDTO.getContactUserId()).isPresent()) {
            throw new ServiceException("Contact already exists");
        }
        Contacts contact = Mapper.mapToContactEntity(contactsDTO);
        contact = contactRepository.save(contact);
        return Mapper.mapToContactDTO(contact);
    }

    @Override
    public List<ContactsDTO> getContacts(String userId) {
        return contactRepository.findAllByUserId(userId)
                .stream()
                .map(Mapper::mapToContactDTO)
                .collect(Collectors.toList());
    }
}
