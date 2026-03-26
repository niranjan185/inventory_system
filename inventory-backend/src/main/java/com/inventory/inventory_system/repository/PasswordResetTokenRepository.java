package com.inventory.inventory_system.repository;

import com.inventory.inventory_system.entity.PasswordResetToken;
import com.inventory.inventory_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    Optional<PasswordResetToken> findByToken(String token);
    
    List<PasswordResetToken> findByUser(User user);
    
    List<PasswordResetToken> findByUserAndUsedFalseOrderByCreatedAtDesc(User user);
    
    @Modifying
    @Query("DELETE FROM PasswordResetToken p WHERE p.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
    
    @Modifying
    @Query("UPDATE PasswordResetToken p SET p.used = true WHERE p.user = :user AND p.used = false")
    void markAllUserTokensAsUsed(@Param("user") User user);
    
    long countByUserAndCreatedAtAfter(User user, LocalDateTime since);
}