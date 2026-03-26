package com.inventory.inventory_system.controller;

import com.inventory.inventory_system.service.OTPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/otp-password-reset")
@CrossOrigin(origins = "*")
public class OTPPasswordResetController {

    @Autowired
    private OTPService otpService;

    // Request OTP for password reset
    @PostMapping("/request-otp")
    public ResponseEntity<?> requestOTP(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }

            String result = otpService.generateAndSendOTP(email.trim().toLowerCase());
            return ResponseEntity.ok(Map.of(
                "message", result,
                "email", email.trim().toLowerCase()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Verify OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String otp = request.get("otp");
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }
            
            if (otp == null || otp.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "OTP is required"));
            }

            boolean isValid = otpService.verifyOTP(email.trim().toLowerCase(), otp.trim());
            
            if (isValid) {
                return ResponseEntity.ok(Map.of(
                    "message", "OTP verified successfully",
                    "verified", true
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid or expired OTP",
                    "verified", false
                ));
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage(),
                "verified", false
            ));
        }
    }

    // Reset password with OTP
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String otp = request.get("otp");
            String newPassword = request.get("newPassword");
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }
            
            if (otp == null || otp.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "OTP is required"));
            }
            
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "New password is required"));
            }
            
            if (newPassword.length() < 6) {
                return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 6 characters long"));
            }

            String result = otpService.resetPasswordWithOTP(
                email.trim().toLowerCase(), 
                otp.trim(), 
                newPassword
            );
            
            return ResponseEntity.ok(Map.of(
                "message", result,
                "success", true
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage(),
                "success", false
            ));
        }
    }

    // Check OTP status (for frontend validation)
    @GetMapping("/otp-status")
    public ResponseEntity<?> getOTPStatus(@RequestParam String email) {
        try {
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }

            long remainingMinutes = otpService.getOTPRemainingMinutes(email.trim().toLowerCase());
            
            return ResponseEntity.ok(Map.of(
                "email", email.trim().toLowerCase(),
                "hasValidOTP", remainingMinutes > 0,
                "remainingMinutes", remainingMinutes
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}