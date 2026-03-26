package com.inventory.inventory_system.service;

import com.inventory.inventory_system.entity.PasswordResetToken;
import com.inventory.inventory_system.entity.User;
import com.inventory.inventory_system.repository.PasswordResetTokenRepository;
import com.inventory.inventory_system.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
@Transactional
public class PasswordResetService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);
    private static final int MAX_RESET_ATTEMPTS_PER_HOUR = 10; // Increased for development

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    private AuditLogService auditLogService;

    private final SecureRandom secureRandom = new SecureRandom();

    // Request password reset
    public boolean requestPasswordReset(String email, String ipAddress) {
        try {
            Optional<User> userOptional = userRepository.findByEmail(email);
            
            if (userOptional.isEmpty()) {
                // Don't reveal if email exists or not for security
                logger.warn("Password reset requested for non-existent email: {} from IP: {}", email, ipAddress);
                return true; // Return true to not reveal email existence
            }

            User user = userOptional.get();

            // Check rate limiting
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            long recentAttempts = passwordResetTokenRepository.countByUserAndCreatedAtAfter(user, oneHourAgo);
            
            if (recentAttempts >= MAX_RESET_ATTEMPTS_PER_HOUR) {
                logger.warn("Too many password reset attempts for user: {} from IP: {}", email, ipAddress);
                throw new RuntimeException("Too many password reset attempts. Please try again later.");
            }

            // Generate secure token
            String token = generateSecureToken();

            // Invalidate existing tokens for this user
            passwordResetTokenRepository.markAllUserTokensAsUsed(user);

            // Create new token
            PasswordResetToken resetToken = new PasswordResetToken(token, user, ipAddress);
            passwordResetTokenRepository.save(resetToken);

            // Send reset email
            emailService.sendPasswordResetEmail(user, token);

            // Log audit
            auditLogService.logAudit("User", user.getId(), "PASSWORD_RESET_REQUESTED", 
                                   user.getEmail(), null, null, ipAddress);

            logger.info("Password reset token generated for user: {}", email);
            return true;

        } catch (Exception e) {
            logger.error("Failed to process password reset request for email: {}", email, e);
            throw new RuntimeException("Failed to process password reset request: " + e.getMessage());
        }
    }

    // Validate reset token
    @Transactional(readOnly = true)
    public boolean validateResetToken(String token) {
        Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findByToken(token);
        return tokenOptional.isPresent() && tokenOptional.get().isValid();
    }

    // Reset password with token
    public boolean resetPassword(String token, String newPassword, String ipAddress) {
        try {
            Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findByToken(token);
            
            if (tokenOptional.isEmpty()) {
                logger.warn("Invalid password reset token used from IP: {}", ipAddress);
                return false;
            }

            PasswordResetToken resetToken = tokenOptional.get();

            if (!resetToken.isValid()) {
                logger.warn("Expired or used password reset token attempted from IP: {}", ipAddress);
                return false;
            }

            User user = resetToken.getUser();
            String oldPasswordHash = user.getPassword();

            // Update password
            String encodedPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encodedPassword);
            userRepository.save(user);

            // Mark token as used
            resetToken.setUsed(true);
            passwordResetTokenRepository.save(resetToken);

            // Send confirmation email
            emailService.sendPasswordResetConfirmationEmail(user);

            // Log audit
            auditLogService.logAudit("User", user.getId(), "PASSWORD_RESET_COMPLETED", 
                                   user.getEmail(), "Password changed", "Password updated", ipAddress);

            logger.info("Password successfully reset for user: {}", user.getEmail());
            return true;

        } catch (Exception e) {
            logger.error("Failed to reset password with token: {}", token, e);
            throw new RuntimeException("Failed to reset password: " + e.getMessage());
        }
    }

    // Get user by reset token (for validation)
    @Transactional(readOnly = true)
    public User getUserByResetToken(String token) {
        Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findByToken(token);
        
        if (tokenOptional.isPresent() && tokenOptional.get().isValid()) {
            return tokenOptional.get().getUser();
        }
        
        return null;
    }

    // Generate secure random token
    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    // Cleanup expired tokens (runs every hour)
    @Scheduled(fixedRate = 3600000) // 1 hour
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            passwordResetTokenRepository.deleteExpiredTokens(LocalDateTime.now());
            logger.debug("Cleaned up expired password reset tokens");
        } catch (Exception e) {
            logger.error("Failed to cleanup expired tokens", e);
        }
    }

    // Admin function to revoke all tokens for a user
    @Transactional
    public void revokeAllUserTokens(String userEmail, String adminEmail, String ipAddress) {
        Optional<User> userOptional = userRepository.findByEmail(userEmail);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            passwordResetTokenRepository.markAllUserTokensAsUsed(user);
            
            auditLogService.logAudit("User", user.getId(), "PASSWORD_RESET_TOKENS_REVOKED", 
                                   adminEmail, null, "All tokens revoked by admin", ipAddress);
            
            logger.info("All password reset tokens revoked for user: {} by admin: {}", userEmail, adminEmail);
        }
    }

    // Development helper - clear all tokens for a user (removes rate limiting)
    @Transactional
    public void clearAllUserTokens(String userEmail) {
        Optional<User> userOptional = userRepository.findByEmail(userEmail);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Mark all tokens as used instead of deleting (safer)
            passwordResetTokenRepository.markAllUserTokensAsUsed(user);
            logger.info("All password reset tokens cleared for user: {} (development)", userEmail);
        }
    }
}