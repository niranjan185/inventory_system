package com.inventory.inventory_system.controller;

import com.inventory.inventory_system.dto.PasswordResetConfirm;
import com.inventory.inventory_system.dto.PasswordResetRequest;
import com.inventory.inventory_system.entity.User;
import com.inventory.inventory_system.service.PasswordResetService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/password-reset")
@CrossOrigin(origins = "*")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    // Request password reset
    @PostMapping("/request")
    public ResponseEntity<Map<String, String>> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequest request,
            HttpServletRequest httpRequest) {
        
        Map<String, String> response = new HashMap<>();
        
        try {
            String clientIP = getClientIP(httpRequest);
            boolean success = passwordResetService.requestPasswordReset(request.getEmail(), clientIP);
            
            if (success) {
                response.put("message", "If the email exists in our system, a password reset link has been sent.");
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Failed to process password reset request");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Validate reset token
    @GetMapping("/validate/{token}")
    public ResponseEntity<Map<String, Object>> validateResetToken(@PathVariable String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isValid = passwordResetService.validateResetToken(token);
            response.put("valid", isValid);
            
            if (isValid) {
                User user = passwordResetService.getUserByResetToken(token);
                if (user != null) {
                    response.put("email", user.getEmail());
                    response.put("name", user.getName());
                }
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("valid", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Reset password with token
    @PostMapping("/confirm")
    public ResponseEntity<Map<String, String>> confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirm request,
            HttpServletRequest httpRequest) {
        
        Map<String, String> response = new HashMap<>();
        
        try {
            // Validate password match
            if (!request.isPasswordMatch()) {
                response.put("error", "Passwords do not match");
                return ResponseEntity.badRequest().body(response);
            }

            String clientIP = getClientIP(httpRequest);
            boolean success = passwordResetService.resetPassword(
                request.getToken(), 
                request.getNewPassword(), 
                clientIP
            );
            
            if (success) {
                response.put("message", "Password has been successfully reset. You can now log in with your new password.");
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Invalid or expired reset token");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Admin endpoint to revoke all tokens for a user
    @PostMapping("/revoke/{userEmail}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> revokeUserTokens(
            @PathVariable String userEmail,
            HttpServletRequest httpRequest) {
        
        Map<String, String> response = new HashMap<>();
        
        try {
            String clientIP = getClientIP(httpRequest);
            // Get admin email from security context would be better, but using hardcoded for now
            passwordResetService.revokeAllUserTokens(userEmail, "admin", clientIP);
            
            response.put("message", "All password reset tokens revoked for user: " + userEmail);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Helper method to get client IP
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    // Development endpoint to generate fresh token (bypasses rate limiting)
    @PostMapping("/dev-reset/{email}")
    public ResponseEntity<Map<String, String>> devGenerateResetToken(@PathVariable String email) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Clear existing tokens first
            passwordResetService.clearAllUserTokens(email);
            
            // Generate new token
            boolean success = passwordResetService.requestPasswordReset(email, "127.0.0.1");
            
            if (success) {
                response.put("message", "Development reset token generated. Check console for link.");
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Failed to generate reset token");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}