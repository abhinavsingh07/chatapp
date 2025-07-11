package com.chatapp.synk.repository;

import com.chatapp.synk.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByPhoneNumber(String phoneNumber);
    List<User> findByNameContainingIgnoreCaseOrPhoneNumberContaining(String namePart, String phonePart);
}