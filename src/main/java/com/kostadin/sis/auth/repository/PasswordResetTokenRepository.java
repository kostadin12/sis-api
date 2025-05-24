package com.kostadin.sis.auth.repository;

import com.kostadin.sis.auth.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenAndUsedFalse(String token);

    List<PasswordResetToken> findByEmailAndUsedFalseOrderByCreatedAtDesc(String email);

    @Query("SELECT COUNT(p) FROM PasswordResetToken p WHERE p.email = :email AND p.createdAt > :since")
    long countRecentRequestsByEmail(@Param("email") String email, @Param("since") LocalDateTime since);

    @Modifying
    @Query("UPDATE PasswordResetToken p SET p.used = true WHERE p.token = :token")
    void markTokenAsUsed(@Param("token") String token);

    @Modifying
    @Query("UPDATE PasswordResetToken p SET p.used = true WHERE p.email = :email AND p.used = false")
    void invalidateAllTokensForEmail(@Param("email") String email);

    @Modifying
    @Query("DELETE FROM PasswordResetToken p WHERE p.expirationTime < :now OR p.createdAt < :cutoff")
    void deleteExpiredTokens(@Param("now") LocalDateTime now, @Param("cutoff") LocalDateTime cutoff);

    @Query("SELECT p FROM PasswordResetToken p WHERE p.expirationTime < :now")
    List<PasswordResetToken> findExpiredTokens(@Param("now") LocalDateTime now);
}