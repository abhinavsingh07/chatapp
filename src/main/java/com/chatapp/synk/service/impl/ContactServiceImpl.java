package com.chatapp.synk.service.impl;

import com.api.emailservice.EmailDTO;
import com.api.emailservice.EmailService;
import com.chatapp.synk.security_validator.InputSecurityUtils;
import com.chatapp.synk.security_validator.InputValidationAndSanitizationService;
import com.chatapp.synk.dto.ContactDTO;
import com.chatapp.synk.dto.ContactUserDTO;
import com.chatapp.synk.dto.ConversationLastMsgDTO;
import com.chatapp.synk.dto.UserDTO;
import com.chatapp.synk.entity.Contact;
import com.chatapp.synk.entity.Media;
import com.chatapp.synk.entity.User;
import com.chatapp.synk.enums.ContactStatus;
import com.chatapp.synk.enums.EmailStatus;
import com.chatapp.synk.exceptionHandler.ServiceException;
import com.chatapp.synk.mediaUpload.enums.MediaUploadStatus;
import com.chatapp.synk.mediaUpload.enums.MediaUsageType;
import com.chatapp.synk.mediaUpload.repository.MediaRepository;
import com.chatapp.synk.repository.ContactRepository;
import com.chatapp.synk.security.SecurityUtil;
import com.chatapp.synk.service.ContactService;
import com.chatapp.synk.service.UserService;
import com.chatapp.synk.util.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Service
public class ContactServiceImpl implements ContactService {
    private static final Logger logger = LoggerFactory.getLogger(ContactServiceImpl.class);
    private final ContactRepository contactRepository;
    private final UserService userService;
    private final EmailService emailService;
    private final ExecutorService taskExecutor;

    public ContactServiceImpl(ContactRepository contactRepository, UserService userService, EmailService emailService,
            ExecutorService taskExecutor) {
        this.contactRepository = contactRepository;
        this.userService = userService;
        this.emailService = emailService;
        this.taskExecutor = taskExecutor;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactUserDTO> getContactsByUserId(String userId, String userDetailsRequired,
            String mediaDetailsRequired) {
        String validId = InputSecurityUtils.secureId(userId);
        if (validId == null || validId.isEmpty()) {
            return List.of();
        }

        List<Contact> contacts = contactRepository.findAllByUserId(Long.parseLong(validId.trim()));
        boolean fetchUser = "true".equalsIgnoreCase(userDetailsRequired);
        boolean fetchMedia = "true".equalsIgnoreCase(mediaDetailsRequired);

        List<ContactUserDTO> result = new ArrayList<>();
        for (Contact contact : contacts) {
            ContactUserDTO dto = new ContactUserDTO();
            dto.setContactId(String.valueOf(contact.getId()));
            dto.setUserId(String.valueOf(contact.getUserId()));
            dto.setContactStatus(contact.getContactStatus());
            dto.setEmailStatus(contact.getEmailStatus());
            dto.setContactEmail(contact.getEmail());
            dto.setIdentifierId(contact.getIdentifierId());

            if (contact.getContactUserId() != null) {
                dto.setContactUserId(String.valueOf(contact.getContactUserId()));

                if (fetchUser) {
                    User user = contact.getContactUser();
                    if (user != null) {
                        dto.setName(user.getName());
                        dto.setPhoneNumber(user.getPhoneNumber());
                        dto.setEmail(user.getEmail());
                        dto.setStatus(user.getStatus());
                    }
                }

                if (fetchMedia) {
                    List<Media> mediaList = contact.getContactUserMedia();
                    if (mediaList != null && !mediaList.isEmpty()) {
                        Long mediaId = mediaList.stream()
                                .filter(m -> m.getStatus() == MediaUploadStatus.ACTIVE
                                        && m.getUsageType() == MediaUsageType.PROFILE_PICTURE)
                                .sorted(Comparator.comparingLong(Media::getId).reversed())
                                .map(Media::getId)
                                .findFirst()
                                .orElse(null);
                        dto.setMediaId(mediaId != null ? String.valueOf(mediaId) : null);
                    }
                }
            }

            result.add(dto);
        }
        return result;
    }

    @Override
    public ContactDTO addContact(ContactDTO dto) {
        ContactDTO validDTO = InputValidationAndSanitizationService.validateAndSanitize(dto);
        String userId = validDTO.getUserId();// userid is of who is adding contact
        String email = validDTO.getEmail();// email is of user to add

        logger.info("Processing addContact request for userId={} with email={}", userId, email);

        try {

            if (contactExistsByEmail(email)) {
                throw new ServiceException("A contact with this email already exists", HttpStatus.BAD_REQUEST);
            }

            // checking if contact email belongs to an existing user from user table
            // it throws 404 if user not found, which we catch to trigger invite flow
            UserDTO existingUser = userService.getUserByPhoneNumberOrEmail(email);

            if (existingUser.getId().equals(userId)) {
                throw new ServiceException("You cannot add yourself as a contact", HttpStatus.BAD_REQUEST);
            }

            // checking if contact already exists for this userId in user table
            if (contactExists(userId, existingUser.getId())) {
                throw new ServiceException("Contact already exists for this user", HttpStatus.BAD_REQUEST);
            }

            // if we reach here, it means email belongs to an existing user and contact
            // doesn't exist, so we can create contact with ADDED status
            ContactDTO contactDTO = new ContactDTO();
            contactDTO.setUserId(userId);
            contactDTO.setContactUserId(existingUser.getId());
            contactDTO.setContactStatus(ContactStatus.ADDED);
            contactDTO.setEmailStatus(EmailStatus.NOT_APPLICABLE);
            contactDTO.setEmail(email);

            return saveContact(contactDTO);

        } catch (ServiceException ex) {
            if (ex.getStatus() == HttpStatus.NOT_FOUND) {
                return handleInviteFlow(userId, email);
            }
            throw ex;
        }
    }

    private ContactDTO handleInviteFlow(String userId, String email) {
        logger.info("Inviting unregistered email={} on behalf of userId={}", email, userId);

        ContactDTO contactDTO = new ContactDTO();
        contactDTO.setUserId(userId);
        contactDTO.setContactUserId(null);
        contactDTO.setContactStatus(ContactStatus.INVITED);
        contactDTO.setEmailStatus(EmailStatus.PENDING);
        contactDTO.setEmail(email);
        ContactDTO savedContact = saveContact(contactDTO);

        // spawning new thread to send email asynchronously to avoid blocking the main
        // thread
        CompletableFuture.runAsync(() -> {
            boolean sent = emailService.sendEmail(new EmailDTO(email, "You're invited to join ChatApp!",
                    "Hi there!\n\nYou've been invited to join ChatApp. "
                            + "Click here to register:\nhttps://yourapp.com/register"));
            updateEmailStatus(savedContact.getId(), sent ? EmailStatus.SENT : EmailStatus.FAILED);
        }, taskExecutor);

        return savedContact;
    }

    private boolean contactExists(String userId, String contactUserId) {
        return !contactRepository.findByUserIdAndContactUserId(Long.parseLong(userId), Long.parseLong(contactUserId))
                .isEmpty();
    }

    private boolean contactExistsByEmail(String email) {
        List<Contact> contacts = contactRepository.findByEmailAndContactUserIdIsNull(email);
        return !contacts.isEmpty();
    }

    private void updateEmailStatus(String contactId, EmailStatus status) {
        contactRepository.findById(Long.parseLong(contactId))
                .ifPresent(contact -> {
                    contact.setEmailStatus(status);
                    contactRepository.save(contact);
                });
    }

    private ContactDTO saveContact(ContactDTO contactDTO) {
        try {
            Contact contactEntity = Mapper.mapToContactEntity(contactDTO);
            // db call to save contact
            Contact saved = contactRepository.save(contactEntity);
            if (logger.isDebugEnabled()) {
                logger.info("Contact saved successfully for userId={}", contactDTO.getUserId());
            }
            return Mapper.mapToContactDTO(saved);

        } catch (Exception ex) {
            logger.error("Failed to save contact for userId={}, reason={}", contactDTO.getUserId(), ex.getMessage(),
                    ex);
            throw new ServiceException("Failed to save contact", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteContact(String contactId) {
        String validContactId = InputSecurityUtils.secureId(contactId);
        Optional<Contact> contactOpt = contactRepository.findById(Long.parseLong(validContactId));

        if (contactOpt.isEmpty()) {
            logger.warn("Delete failed - no contact found with ID: {}", validContactId);
            throw new ServiceException("Contact not found for given contact id", HttpStatus.NOT_FOUND);
        }

        Contact contact = contactOpt.get();
        // delete from DB
        contactRepository.delete(contact);

        logger.info("Contact deleted successfully: {}", validContactId);
    }

}
