package com.inventory.inventory_system.controller;

import com.inventory.inventory_system.entity.Alert;
import com.inventory.inventory_system.service.AlertService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "*")
public class AlertController {

    @Autowired
    private AlertService alertService;

    // Get all alerts (Admin and User can view with role-appropriate messages)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public List<Alert> getAllAlerts(Authentication authentication) {
        List<Alert> alerts = alertService.getAllAlerts();
        
        // Transform messages based on user role
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            
        if (!isAdmin) {
            // Transform admin messages to user-friendly messages for USER role
            alerts = alerts.stream().map(alert -> {
                // Convert admin message to user-friendly message
                String userMessage = convertToUserFriendlyMessage(alert.getMessage(), alert.getProductName());
                
                // Create new Alert with user-friendly message
                return new Alert(alert.getId(), alert.getProductName(), userMessage, alert.getCreatedAt());
            }).collect(Collectors.toList());
        }
        
        return alerts;
    }

    // Get alerts by product name (Admin and User can view with role-appropriate messages)
    @GetMapping("/product/{productName}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public List<Alert> getAlertsByProduct(@PathVariable String productName, Authentication authentication) {
        List<Alert> alerts = alertService.getAlertsByProductName(productName);
        
        // Transform messages based on user role
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            
        if (!isAdmin) {
            // Transform admin messages to user-friendly messages for USER role
            alerts = alerts.stream().map(alert -> {
                // Convert admin message to user-friendly message
                String userMessage = convertToUserFriendlyMessage(alert.getMessage(), alert.getProductName());
                
                // Create new Alert with user-friendly message
                return new Alert(alert.getId(), alert.getProductName(), userMessage, alert.getCreatedAt());
            }).collect(Collectors.toList());
        }
        
        return alerts;
    }

    // Create manual alert
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Alert createAlert(@RequestBody Alert alert) {
        return alertService.createAlert(alert);
    }

    // Delete alert
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteAlert(@PathVariable Long id) {
        alertService.deleteAlert(id);
        return ResponseEntity.ok("Alert deleted successfully");
    }

    // Refresh alert messages (update existing alerts with new user-friendly messages)
    @PostMapping("/refresh-messages")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> refreshAlertMessages() {
        alertService.refreshAlertMessages();
        return ResponseEntity.ok("Alert messages refreshed successfully");
    }

    // Mark all alerts as read (delete all)
    @DeleteMapping("/clear")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> clearAllAlerts() {
        alertService.clearAllAlerts();
        return ResponseEntity.ok("All alerts cleared successfully");
    }
    
    // Helper method to convert admin messages to user-friendly messages
    private String convertToUserFriendlyMessage(String adminMessage, String productName) {
        try {
            // Extract quantity from admin message
            int quantity = 0;
            if (adminMessage.contains("Only ")) {
                String[] parts = adminMessage.split("Only ");
                if (parts.length > 1) {
                    String numberPart = parts[1].split(" ")[0];
                    quantity = Integer.parseInt(numberPart);
                }
            }
            
            // Generate user-friendly message
            if (quantity == 0 || adminMessage.contains("out of stock")) {
                return "Low stock alert: " + productName + " is out of stock. Please grab it when available!";
            } else {
                return "Low stock alert: Only " + quantity + " " + productName + " remaining. Please grab it fast!";
            }
        } catch (Exception e) {
            // Fallback to a generic user-friendly message
            return "Low stock alert: " + productName + " stock is running low. Please grab it fast!";
        }
    }
}