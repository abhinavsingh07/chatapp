package com.chatapp.synk.service.impl;

import com.chatapp.synk.dto.ContactDTO;
import com.chatapp.synk.dto.UserDTO;
import com.chatapp.synk.entity.Contact;
import com.chatapp.synk.exceptionHandler.ServiceException;
import com.chatapp.synk.repository.ContactRepository;
import com.chatapp.synk.service.ContactService;
import com.chatapp.synk.service.UserService;
import com.chatapp.synk.util.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ContactServiceImpl implements ContactService {
    private static final Logger logger = LoggerFactory.getLogger(ContactServiceImpl.class);
    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private UserService userService;

    @Override
    @CachePut(value = "contactCache", key = "#result.id", unless = "#result == null")
    public ContactDTO addContact(ContactDTO contactDTO) {
        logger.info("Attempting to add contact: userId={}, contactUserId={}", contactDTO.getUserId(), contactDTO.getContactUserId());

        Optional<ContactDTO> existingContact = contactRepository.findByUserIdAndContactUserId(contactDTO.getUserId(), contactDTO.getContactUserId());

        if (existingContact.isPresent()) {
            logger.warn("Contact already exists for userId={} and contactUserId={}", contactDTO.getUserId(), contactDTO.getContactUserId());
            throw new ServiceException("Contact already exists for this user. Please try again with a different contact.");
        }

        logger.debug("No existing contact found. Proceeding to save new contact.");

        Contact contactEntity = Mapper.mapToContactEntity(contactDTO);
        Contact savedContact = contactRepository.save(contactEntity);

        logger.info("Contact saved successfully with ID: {}", savedContact.getId());

        ContactDTO savedDTO = Mapper.mapToContactDTO(savedContact);

        return savedDTO;
    }


    @Override
    @Cacheable(value = "contactCache", key = "'contactsForUser_' + #userId")
    public List<UserDTO> getContactsByUserId(String userId) {
        logger.info("Fetching contacts for userId: {}", userId);

        List<ContactDTO> contactDTOList = contactRepository.findAllByUserId(userId);
        logger.debug("Found {} contacts for userId {}", contactDTOList.size(), userId);

        List<UserDTO> userDTOList = contactDTOList.stream().map(contactDTO -> {
            String contactUserId = contactDTO.getUserId();
            logger.debug("Fetching UserDTO for contactUserId: {}", contactUserId);
            UserDTO userDTO = userService.getUserById(contactUserId);
            if (userDTO == null) {
                logger.warn("UserDTO not found for contactUserId: {}", contactUserId);
            }
            return userDTO;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        logger.info("Returning {} UserDTOs for userId {}", userDTOList.size(), userId);
        return userDTOList;
    }

    @Override
    @CacheEvict(value = "contactCache", key = "#contactId")
    public void deleteContact(String contactId) {
        logger.info("Attempting to delete contact with ID: {}", contactId);

        Optional<Contact> contactOpt = contactRepository.findById(contactId);
        if (contactOpt.isEmpty()) {
            logger.warn("No contact found with ID: {}", contactId);
            throw new ServiceException("Contact not found");
        }

        Contact contact = contactOpt.get();
        contactRepository.delete(contact);
        logger.info("Contact with ID {} deleted successfully", contactId);
    }

}
