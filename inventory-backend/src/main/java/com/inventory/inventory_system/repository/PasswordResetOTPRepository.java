package com.inventory.inventory_system.repository;

import com.inventory.inventory_system.entity.PasswordResetOTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetOTPRepository extends JpaRepository<PasswordResetOTP, Long> {

    // Find the latest valid OTP for an email
    @Query("SELECT o FROM PasswordResetOTP o WHERE o.email = :email AND o.used = false AND o.expiresAt > :now ORDER BY o.createdAt DESC")
    Optional<PasswordResetOTP> findLatestValidOTPByEmail(@Param("email") String email, @Param("now") LocalDateTime now);

    // Find OTP by email and OTP code
    Optional<PasswordResetOTP> findByEmailAndOtpAndUsedFalse(String email, String otp);

    // Find all OTPs for an email (for rate limiting)
    List<PasswordResetOTP> findByEmailAndCreatedAtAfter(String email, LocalDateTime after);

    // Mark OTP as used
    @Modifying
    @Transactional
    @Query("UPDATE PasswordResetOTP o SET o.used = true WHERE o.id = :id")
    void markAsUsed(@Param("id") Long id);

    // Clean up expired OTPs (for maintenance)
    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetOTP o WHERE o.expiresAt < :now OR o.used = true")
    void deleteExpiredAndUsedOTPs(@Param("now") LocalDateTime now);

    // Count recent OTP requests for rate limiting
    @Query("SELECT COUNT(o) FROM PasswordResetOTP o WHERE o.email = :email AND o.createdAt > :since")
    long countRecentOTPRequests(@Param("email") String email, @Param("since") LocalDateTime since);
}