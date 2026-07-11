package com.chatapp.synk.repository;

import com.chatapp.synk.dto.ContactUserDTO;
import com.chatapp.synk.entity.Contact;
import com.chatapp.synk.enums.ContactStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    List<Contact> findByUserIdAndContactUserId(Long userId, Long contactUserId);

    List<Contact> findAllByUserId(Long userId);

    List<Contact> findByEmailAndContactUserIdIsNull(String email);

    @Modifying
    @Query("""
            UPDATE Contact c
            SET c.contactUserId = :userId,
                c.contactStatus = :contactStatus
            WHERE c.email = :email
              AND c.contactUserId IS NULL
            """)
    int updateContactUserIdByEmail(
            @Param("userId") Long userId,
            @Param("contactStatus") ContactStatus contactStatus,
            @Param("email") String email);
}
