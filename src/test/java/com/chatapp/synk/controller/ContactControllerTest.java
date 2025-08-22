package com.chatapp.synk.controller;

import com.chatapp.synk.dto.ContactDTO;
import com.chatapp.synk.dto.ContactUserDTO;
import com.chatapp.synk.enums.ContactStatus;
import com.chatapp.synk.response.SuccessResponse;
import com.chatapp.synk.service.ContactService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactControllerTest {

    @InjectMocks
    private ContactController contactController;

    @Mock
    private ContactService contactService;

    @Test
    void testGetContacts_Found() {
        // Arrange
        String userId = "123";
        ContactUserDTO contactUser = new ContactUserDTO();
        contactUser.setContactId("1");
        contactUser.setName("Alice");

        when(contactService.getContacts(userId)).thenReturn(List.of(contactUser));

        // Act
        ResponseEntity<SuccessResponse<ContactUserDTO>> response = contactController.getContacts(userId);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("200", response.getBody().getResponseCode());
        assertEquals(1, response.getBody().getData().size());
        assertEquals("Alice", response.getBody().getData().get(0).getName());
    }

    @Test
    void testGetContacts_NotFound() {
        // Arrange
        String userId = "123";
        when(contactService.getContacts(userId)).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<SuccessResponse<ContactUserDTO>> response = contactController.getContacts(userId);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("404", response.getBody().getResponseCode());
        assertEquals("No contacts found", response.getBody().getMessage());
        assertEquals(0, response.getBody().getData().size());
    }

    @Test
    void testAddContact_Added() {
        // Arrange
        ContactDTO inputContact = new ContactDTO();
        inputContact.setId("1");
        inputContact.setEmail("Bob");

        ContactDTO savedContact = new ContactDTO();
        savedContact.setId("1");
        savedContact.setEmail("Bob");
        savedContact.setContactStatus(ContactStatus.ADDED);

        when(contactService.addContact(any(ContactDTO.class))).thenReturn(savedContact);

        // Act
        ResponseEntity<SuccessResponse<ContactDTO>> response = contactController.addContact(inputContact);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("200", response.getBody().getResponseCode());
        assertEquals("Contact created successfully", response.getBody().getMessage());
        assertEquals("Bob", response.getBody().getData().get(0).getEmail());
    }

    @Test
    void testAddContact_Invited() {
        // Arrange
        ContactDTO inputContact = new ContactDTO();
        inputContact.setId("2");
        inputContact.setEmail("Charlie");

        ContactDTO savedContact = new ContactDTO();
        savedContact.setId("2");
        savedContact.setEmail("Charlie");
        savedContact.setContactStatus(ContactStatus.INVITED);

        when(contactService.addContact(any(ContactDTO.class))).thenReturn(savedContact);

        // Act
        ResponseEntity<SuccessResponse<ContactDTO>> response = contactController.addContact(inputContact);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Invitation sent successfully", response.getBody().getMessage());
    }

    @Test
    void testDeleteContact() {
        // Arrange
        String contactId = "1";
        doNothing().when(contactService).deleteContact(contactId);

        // Act
        ResponseEntity<SuccessResponse<String>> response = contactController.deleteContact(contactId);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("200", response.getBody().getResponseCode());
        assertEquals("Contact deleted successfully", response.getBody().getMessage());
    }
}
