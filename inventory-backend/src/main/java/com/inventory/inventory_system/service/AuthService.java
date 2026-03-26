package com.inventory.inventory_system.service;

import com.inventory.inventory_system.entity.User;
import com.inventory.inventory_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuditLogService auditLogService;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10; // 10 minutes as requested

    /**
     * Send OTP to user's email for password reset
     */
    @Transactional
    public String sendOtpToEmail(String email) {
        // Validate email format
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }

        email = email.trim().toLowerCase();

        // Check if user exists
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("No account found with this email address");
        }

        User user = userOpt.get();

        // Generate 6-digit OTP
        String otp = generateOTP();
        LocalDateTime otpExpiry = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

        // Update user with OTP and expiry
        user.setOtp(otp);
        user.setOtpExpiry(otpExpiry);
        userRepository.save(user);

        // Send OTP via email
        try {
            emailService.sendOTPEmail(email, user.getName(), otp);
            
            // Log the action
            auditLogService.logAudit("User", user.getId(), "OTP_SENT", email, null, 
                "OTP sent for password reset", "SYSTEM");
            
            return "OTP sent successfully to " + email;
        } catch (Exception e) {
            // Clear OTP if email sending fails
            user.clearOtp();
            userRepository.save(user);
            throw new RuntimeException("Failed to send OTP email. Please try again.");
        }
    }

    /**
     * Verify OTP for password reset
     */
    @Transactional
    public boolean verifyOtp(String email, String otp) {
        if (email == null || email.trim().isEmpty() || otp == null || otp.trim().isEmpty()) {
            return false;
        }

        email = email.trim().toLowerCase();
        otp = otp.trim();

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();

        // Check if OTP matches and is not expired
        if (user.getOtp() != null && user.getOtp().equals(otp) && user.isOtpValid()) {
            // Log successful verification
            auditLogService.logAudit("User", user.getId(), "OTP_VERIFIED", email, null, 
                "OTP verified successfully", "SYSTEM");
            return true;
        } else {
            // Log failed verification
            auditLogService.logAudit("User", user.getId(), "OTP_VERIFICATION_FAILED", email, null, 
                "Invalid or expired OTP", "SYSTEM");
            return false;
        }
    }

    /**
     * Reset password after OTP verification
     */
    @Transactional
    public String resetPassword(String email, String otp, String newPassword) {
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        
        if (otp == null || otp.trim().isEmpty()) {
            throw new RuntimeException("OTP is required");
        }
        
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new RuntimeException("New password is required");
        }
        
        if (newPassword.length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters long");
        }

        email = email.trim().toLowerCase();
        otp = otp.trim();

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();

        // Verify OTP one more time
        if (user.getOtp() == null || !user.getOtp().equals(otp) || !user.isOtpValid()) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        
        // Clear OTP data after successful reset
        user.clearOtp();
        
        userRepository.save(user);

        // Log the action
        auditLogService.logAudit("User", user.getId(), "PASSWORD_RESET_COMPLETED", email, null, 
            "Password reset completed successfully", user.getEmail());

        return "Password reset successfully";
    }

    /**
     * Generate random 6-digit OTP
     */
    private String generateOTP() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        
        return otp.toString();
    }

    /**
     * Check if user has valid OTP (for frontend validation)
     */
    public boolean hasValidOtp(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        Optional<User> userOpt = userRepository.findByEmail(email.trim().toLowerCase());
        return userOpt.isPresent() && userOpt.get().isOtpValid();
    }

    /**
     * Get remaining OTP validity time in minutes
     */
    public long getOtpRemainingMinutes(String email) {
        if (email == null || email.trim().isEmpty()) {
            return 0;
        }

        Optional<User> userOpt = userRepository.findByEmail(email.trim().toLowerCase());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.isOtpValid()) {
                return java.time.Duration.between(LocalDateTime.now(), user.getOtpExpiry()).toMinutes();
            }
        }
        
        return 0;
    }
}