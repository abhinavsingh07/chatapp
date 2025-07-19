package com.chatapp.synk.repository;

import com.chatapp.synk.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, String> {
    Optional<Users> findByPhoneNumber(String phoneNumber);
    List<Users> findByNameContainingIgnoreCaseOrPhoneNumberContaining(String namePart, String phonePart);
}