package com.chatapp.synk.repository;

import com.chatapp.synk.entity.UserRole;
import com.chatapp.synk.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, String> {
    Optional<UserRole> findByName(RoleName name);
}
