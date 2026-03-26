package com.inventory.inventory_system.service;

import com.inventory.inventory_system.entity.Alert;
import com.inventory.inventory_system.repository.AlertRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlertService {

    @Autowired
    private AlertRepository alertRepository;

    // Get all alerts
    public List<Alert> getAllAlerts() {
        return alertRepository.findAllByOrderByCreatedAtDesc();
    }

    // Get alerts by product name
    public List<Alert> getAlertsByProductName(String productName) {
        return alertRepository.findByProductNameContainingIgnoreCaseOrderByCreatedAtDesc(productName);
    }

    // Create alert
    public Alert createAlert(Alert alert) {
        alert.setCreatedAt(LocalDateTime.now());
        return alertRepository.save(alert);
    }

    // Create low stock alert (called by ProductService)
    public Alert createLowStockAlert(String productName, int quantity) {
        Alert alert = new Alert();
        alert.setProductName(productName);

        // Store admin-focused message as the default message
        String message;
        if (quantity == 0) {
            message = "Low stock alert: " + productName + " is out of stock. Please restock immediately.";
        } else {
            message = "Low stock alert: Only " + quantity + " items remaining. Please restock immediately.";
        }

        alert.setMessage(message);
        alert.setCreatedAt(LocalDateTime.now());
        return alertRepository.save(alert);
    }


    // Delete alert
    public void deleteAlert(Long id) {
        alertRepository.deleteById(id);
    }

    // Clear all alerts
    public void clearAllAlerts() {
        alertRepository.deleteAll();
    }
    
    // Refresh existing alert messages to be admin-focused
    public void refreshAlertMessages() {
        List<Alert> allAlerts = alertRepository.findAll();
        
        for (Alert alert : allAlerts) {
            String message = alert.getMessage();
            
            // Check if it's an old user-friendly alert message
            if (message.contains("Please grab it fast") || message.contains("Please grab it when available")) {
                // Extract quantity from the old message
                String productName = alert.getProductName();
                int quantity = extractQuantityFromMessage(message);
                
                // Generate new admin-focused message
                String newMessage;
                if (quantity == 0 || message.contains("out of stock")) {
                    newMessage = "Low stock alert: " + productName + " is out of stock. Please restock immediately.";
                } else {
                    newMessage = "Low stock alert: Only " + quantity + " items remaining. Please restock immediately.";
                }
                
                alert.setMessage(newMessage);
                alertRepository.save(alert);
            }
        }
    }
    
    // Helper method to extract quantity from alert messages
    private int extractQuantityFromMessage(String message) {
        try {
            // Extract number from "Only X items remaining" or "Only X productName remaining"
            String[] parts = message.split("Only ");
            if (parts.length > 1) {
                String numberPart = parts[1].split(" ")[0];
                return Integer.parseInt(numberPart);
            }
        } catch (Exception e) {
            // If parsing fails, default to 0
        }
        return 0;
    }
}