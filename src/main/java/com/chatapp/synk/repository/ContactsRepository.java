package com.chatapp.synk.repository;

import com.chatapp.synk.entity.Contacts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface ContactsRepository extends JpaRepository<Contacts, String> {
    Optional<Contacts> findByUserIdAndContactUserId(String userId, String contactUserId);

    List<Contacts> findAllByUserId(String userId);
}
