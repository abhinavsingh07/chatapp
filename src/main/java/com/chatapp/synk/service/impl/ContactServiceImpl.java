package com.chatapp.synk.service.impl;

import com.chatapp.synk.dto.ContactDTO;
import com.chatapp.synk.entity.Contact;
import com.chatapp.synk.exceptionHandler.ServiceException;
import com.chatapp.synk.repository.ContactRepository;
import com.chatapp.synk.service.ContactService;
import com.chatapp.synk.util.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ContactServiceImpl implements ContactService {

    @Autowired
    private ContactRepository contactRepository;

    @Override
    @CachePut(value = "contactCache", key = "#result.id", unless = "#result == null")
    public ContactDTO addContact(ContactDTO contactDTO) {
        Optional<ContactDTO> contactDto = contactRepository.findByUserIdAndContactUserId(contactDTO.getUserId(), contactDTO.getContactUserId());
        if (contactDto.isPresent()) {
            throw new ServiceException("Contact already exists");
        }
        if (!contactDto.isEmpty()) {
            Contact contact = Mapper.mapToContactEntity(contactDTO);
            contact = contactRepository.save(contact);
            return Mapper.mapToContactDTO(contact);
        }
        return null;
    }

    @Override
    @Cacheable(value = "contactCache", key = "'allContacts'")
    public List<ContactDTO> getContactsByUserId(String userId) {
        return contactRepository.findAllByUserId(userId).stream().map(Mapper::mapToContactDTO).collect(Collectors.toList());
    }
}
