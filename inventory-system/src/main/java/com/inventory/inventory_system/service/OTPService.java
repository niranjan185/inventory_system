package com.inventory.inventory_system.service;

import com.inventory.inventory_system.entity.PasswordResetOTP;
import com.inventory.inventory_system.entity.User;
import com.inventory.inventory_system.repository.PasswordResetOTPRepository;
import com.inventory.inventory_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OTPService {

    @Autowired
    private PasswordResetOTPRepository otpRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 15; // OTP expires in 15 minutes
    private static final int MAX_OTP_REQUESTS_PER_HOUR = 5;
    private static final int MAX_OTP_ATTEMPTS = 3;

    // Generate and send OTP for password reset
    @Transactional
    public String generateAndSendOTP(String email) {
        // Check if user exists
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with email: " + email);
        }

        User user = userOpt.get();

        // Check rate limiting - max 5 OTP requests per hour
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentRequests = otpRepository.countRecentOTPRequests(email, oneHourAgo);
        
        if (recentRequests >= MAX_OTP_REQUESTS_PER_HOUR) {
            throw new RuntimeException("Too many OTP requests. Please try again later.");
        }

        // Generate 6-digit OTP
        String otp = generateOTP();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

        // Invalidate any existing valid OTPs for this email
        invalidateExistingOTPs(email);

        // Save new OTP
        PasswordResetOTP otpEntity = new PasswordResetOTP(email, otp, expiresAt);
        otpRepository.save(otpEntity);

        // Send OTP via email
        try {
            emailService.sendOTPEmail(email, user.getName(), otp);
            
            // Log the action
            auditLogService.logAudit("User", null, "OTP_REQUESTED", email, null, 
                "OTP requested for password reset", "SYSTEM");
            
            return "OTP sent successfully to " + email;
        } catch (Exception e) {
            throw new RuntimeException("Failed to send OTP email: " + e.getMessage());
        }
    }

    // Verify OTP
    @Transactional
    public boolean verifyOTP(String email, String otp) {
        Optional<PasswordResetOTP> otpEntityOpt = otpRepository.findByEmailAndOtpAndUsedFalse(email, otp);
        
        if (otpEntityOpt.isEmpty()) {
            // Log failed attempt
            auditLogService.logAudit("User", null, "OTP_VERIFICATION_FAILED", email, null, 
                "Invalid OTP attempt", "SYSTEM");
            return false;
        }

        PasswordResetOTP otpEntity = otpEntityOpt.get();

        // Increment attempts
        otpEntity.incrementAttempts();
        otpRepository.save(otpEntity);

        // Check if OTP is still valid
        if (!otpEntity.isValid()) {
            auditLogService.logAudit("User", null, "OTP_VERIFICATION_FAILED", email, null, 
                "Expired or invalid OTP attempt", "SYSTEM");
            return false;
        }

        // OTP is valid
        auditLogService.logAudit("User", null, "OTP_VERIFIED", email, null, 
            "OTP successfully verified", "SYSTEM");
        
        return true;
    }

    // Reset password with OTP verification
    @Transactional
    public String resetPasswordWithOTP(String email, String otp, String newPassword) {
        // First verify the OTP
        Optional<PasswordResetOTP> otpEntityOpt = otpRepository.findByEmailAndOtpAndUsedFalse(email, otp);
        
        if (otpEntityOpt.isEmpty()) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        PasswordResetOTP otpEntity = otpEntityOpt.get();

        if (!otpEntity.isValid()) {
            throw new RuntimeException("OTP has expired or exceeded maximum attempts");
        }

        // Find user
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark OTP as used
        otpEntity.setUsed(true);
        otpRepository.save(otpEntity);

        // Log the action
        auditLogService.logAudit("User", user.getId(), "PASSWORD_RESET_COMPLETED", email, null, 
            "Password reset completed", user.getEmail());

        return "Password reset successfully";
    }

    // Generate random 6-digit OTP
    private String generateOTP() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        
        return otp.toString();
    }

    // Invalidate existing valid OTPs for an email
    private void invalidateExistingOTPs(String email) {
        LocalDateTime now = LocalDateTime.now();
        Optional<PasswordResetOTP> existingOTP = otpRepository.findLatestValidOTPByEmail(email, now);
        
        if (existingOTP.isPresent()) {
            PasswordResetOTP otp = existingOTP.get();
            otp.setUsed(true);
            otpRepository.save(otp);
        }
    }

    // Check if OTP exists and is valid (for frontend validation)
    public boolean isOTPValid(String email, String otp) {
        Optional<PasswordResetOTP> otpEntityOpt = otpRepository.findByEmailAndOtpAndUsedFalse(email, otp);
        return otpEntityOpt.isPresent() && otpEntityOpt.get().isValid();
    }

    // Get remaining time for OTP expiry
    public long getOTPRemainingMinutes(String email) {
        LocalDateTime now = LocalDateTime.now();
        Optional<PasswordResetOTP> otpOpt = otpRepository.findLatestValidOTPByEmail(email, now);
        
        if (otpOpt.isPresent()) {
            PasswordResetOTP otp = otpOpt.get();
            if (otp.isValid()) {
                return java.time.Duration.between(now, otp.getExpiresAt()).toMinutes();
            }
        }
        
        return 0;
    }

    // Scheduled cleanup of expired OTPs (runs every hour)
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    @Transactional
    public void cleanupExpiredOTPs() {
        LocalDateTime now = LocalDateTime.now();
        otpRepository.deleteExpiredAndUsedOTPs(now);
    }
}