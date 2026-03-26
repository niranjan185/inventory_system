package com.inventory.inventory_system.controller;

import com.inventory.inventory_system.entity.NotificationPreference;
import com.inventory.inventory_system.service.NotificationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // Get user's notification preferences
    @GetMapping("/preferences")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getNotificationPreferences(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            System.out.println("Fetching notification preferences for user: " + userEmail);
            NotificationPreference preferences = notificationService.getUserPreferences(userEmail);
            return ResponseEntity.ok(preferences);
        } catch (Exception e) {
            System.err.println("Error fetching notification preferences for user " + authentication.getName() + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Update user's notification preferences
    @PutMapping("/preferences")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> updateNotificationPreferences(
            @RequestBody NotificationPreference preferences,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            NotificationPreference updated = notificationService.updateUserPreferences(userEmail, preferences);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Test email functionality (Admin only)
    @PostMapping("/test-email")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> testEmail(@RequestParam String email, @RequestParam String type) {
        try {
            notificationService.sendTestEmail(email, type);
            return ResponseEntity.ok("Test email sent successfully to: " + email);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error sending test email: " + e.getMessage());
        }
    }
}