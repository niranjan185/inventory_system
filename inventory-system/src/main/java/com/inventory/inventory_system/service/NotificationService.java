package com.inventory.inventory_system.service;

import com.inventory.inventory_system.entity.NotificationPreference;
import com.inventory.inventory_system.entity.User;
import com.inventory.inventory_system.repository.NotificationPreferenceRepository;
import com.inventory.inventory_system.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class NotificationService {

    @Autowired
    private NotificationPreferenceRepository notificationPreferenceRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JavaMailSender mailSender;

    public NotificationPreference getUserPreferences(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Optional<NotificationPreference> existingPrefs = notificationPreferenceRepository.findByUser(user);
        
        if (existingPrefs.isPresent()) {
            return existingPrefs.get();
        }
        
        // Check if there's already a preference record for this user ID (corrupted data)
        Optional<NotificationPreference> existingById = notificationPreferenceRepository.findByUserId(user.getId());
        if (existingById.isPresent()) {
            System.out.println("Found existing notification preference for user ID: " + user.getId() + ", returning it");
            return existingById.get();
        }
        
        // Create new preferences only if none exist
        try {
            return createDefaultPreferences(user);
        } catch (Exception e) {
            // If creation fails due to constraint violation, try to find existing record again
            System.err.println("Failed to create default preferences for user " + userEmail + ": " + e.getMessage());
            Optional<NotificationPreference> retryFind = notificationPreferenceRepository.findByUserId(user.getId());
            if (retryFind.isPresent()) {
                System.out.println("Found existing preference after creation failure, returning it");
                return retryFind.get();
            }
            throw new RuntimeException("Unable to get or create notification preferences for user: " + userEmail, e);
        }
    }

    public NotificationPreference updateUserPreferences(String userEmail, NotificationPreference newPreferences) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Optional<NotificationPreference> existingPrefs = notificationPreferenceRepository.findByUser(user);
        
        NotificationPreference preferences;
        if (existingPrefs.isPresent()) {
            preferences = existingPrefs.get();
        } else {
            preferences = new NotificationPreference(user);
        }
        
        preferences.setLowStockAlerts(newPreferences.isLowStockAlerts());
        preferences.setWelcomeEmails(newPreferences.isWelcomeEmails());
        preferences.setStockRestoredAlerts(newPreferences.isStockRestoredAlerts());
        
        return notificationPreferenceRepository.save(preferences);
    }

    private NotificationPreference createDefaultPreferences(User user) {
        try {
            NotificationPreference defaultPrefs = new NotificationPreference(user);
            return notificationPreferenceRepository.save(defaultPrefs);
        } catch (Exception e) {
            System.err.println("Error creating default preferences for user " + user.getEmail() + ": " + e.getMessage());
            throw e;
        }
    }

    public void sendTestEmail(String email, String type) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        
        switch (type.toLowerCase()) {
            case "welcome":
                message.setSubject("Test Welcome Email");
                message.setText("This is a test welcome email from the Inventory Management System.");
                break;
            case "lowstock":
                message.setSubject("Test Low Stock Alert");
                message.setText("This is a test low stock alert email from the Inventory Management System.");
                break;
            case "restored":
                message.setSubject("Test Stock Restored Alert");
                message.setText("This is a test stock restored email from the Inventory Management System.");
                break;
            default:
                message.setSubject("Test Email");
                message.setText("This is a test email from the Inventory Management System.");
        }
        
        mailSender.send(message);
    }
}