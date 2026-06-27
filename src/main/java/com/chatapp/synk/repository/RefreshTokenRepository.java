package com.chatapp.synk.repository;

import com.chatapp.synk.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query(value = """
            UPDATE refresh_tokens
            SET revoked = 'Y',
                revoked_at = CURRENT_TIMESTAMP,
                revoke_reason = 'LOGOUT'
            WHERE token_hash = :tokenHash
            """, nativeQuery = true)
    int revokeToken(@Param("tokenHash") String tokenHash);
}