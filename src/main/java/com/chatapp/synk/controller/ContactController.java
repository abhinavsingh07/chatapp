package com.chatapp.synk.controller;

import com.chatapp.synk.dto.ContactsDTO;
import com.chatapp.synk.response.SuccessResponse;
import com.chatapp.synk.service.ContactsService;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final ContactsService contactService;

    @Autowired
    public ContactController(ContactsService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    public ResponseEntity<SuccessResponse<ContactsDTO>> addContact(@Valid @RequestBody ContactsDTO contactsDTO) {
        logger.info("Creating contact for given userid..");
        ContactsDTO contact = contactService.addContact(contactsDTO);
        return ResponseEntity.ok(new SuccessResponse<>("200", "Contact created successfully", List.of(contact)));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<SuccessResponse<ContactsDTO>> getContactsBYUserId(@PathVariable(required = true) String userId) {
        logger.info("Received contact search request for userId: {}", userId);

        List<ContactsDTO> results = contactService.getContacts(userId);

        if (results.isEmpty()) {
            logger.warn("No contacts found for userId: {}", userId);
        } else {
            logger.info("Found {} contacts for userId: {}", results.size(), userId);
        }

        String msg = results.isEmpty() ? "No contacts found" : "Contact list retrieved";
        String code = results.isEmpty() ? "204" : "200";

        return ResponseEntity.ok(new SuccessResponse<>(code, msg, results));
    }
}