package com.chatapp.synk.repository;

import com.chatapp.synk.dto.ContactDTO;
import com.chatapp.synk.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, String> {
    Optional<ContactDTO> findByUserIdAndContactUserId(String userId, String contactUserId);

    List<ContactDTO> findAllByUserId(String userId);
}
