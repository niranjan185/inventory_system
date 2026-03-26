package com.inventory.inventory_system.service;

import com.inventory.inventory_system.entity.NotificationPreference;
import com.inventory.inventory_system.entity.User;
import com.inventory.inventory_system.repository.NotificationPreferenceRepository;
import com.inventory.inventory_system.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationPreferenceRepository notificationPreferenceRepository;
    
    @Value("${spring.mail.enabled:true}")
    private boolean emailEnabled;

    @Async("emailTaskExecutor")
    public void sendLowStockAlert(String productName, int quantity) {
        logger.info("Sending async low stock alert for product: {} (quantity: {})", productName, quantity);
        
        if (!emailEnabled) {
            logger.info("Email disabled - Low stock alert would be sent for: {} (quantity: {})", productName, quantity);
            return;
        }
        
        try {
            // Send notification to admin
            sendAdminLowStockAlert(productName, quantity);
            
            // Send notification to users who have enabled low stock alerts
            sendUserLowStockAlert(productName, quantity);
            
            logger.info("Low stock alert emails sent successfully for: {}", productName);
        } catch (Exception e) {
            logger.error("Failed to send low stock alert emails for {}: {}", productName, e.getMessage());
        }
    }
    
    private void sendAdminLowStockAlert(String productName, int quantity) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("admin@inventory.com"); // Admin email
        message.setSubject("🚨 URGENT: Low Stock Alert - Restock Required");

        message.setText(
                "ADMIN ALERT - IMMEDIATE ACTION REQUIRED\n\n" +
                "Product: " + productName + "\n" +
                "Current Stock: " + quantity + " units\n" +
                "Status: CRITICAL LOW STOCK\n\n" +
                "ACTION REQUIRED:\n" +
                "• Please restock this item immediately\n" +
                "• Check supplier availability\n" +
                "• Update reorder levels if necessary\n\n" +
                "This is an automated alert from the Inventory Management System.\n" +
                "Please take immediate action to prevent stockouts."
        );

        try {
            mailSender.send(message);
            logger.debug("Admin low stock alert sent for: {}", productName);
        } catch (Exception e) {
            logger.error("Failed to send admin alert for {}: {}", productName, e.getMessage());
        }
    }
    
    private void sendUserLowStockAlert(String productName, int quantity) {
        // Get all users to notify them
        List<User> allUsers = userRepository.findAll();
        
        for (User user : allUsers) {
            // Check user's notification preferences
            NotificationPreference preferences = notificationPreferenceRepository.findByUser(user)
                    .orElse(new NotificationPreference(user)); // Default preferences if not set
            
            if (preferences.isLowStockAlerts()) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(user.getEmail());
                message.setSubject("⚡ Limited Stock Alert - " + productName);

                message.setText(
                        "Hello " + user.getName() + ",\n\n" +
                        "HURRY! Limited stock available!\n\n" +
                        "Product: " + productName + "\n" +
                        "Only " + quantity + " units left in stock!\n\n" +
                        "🔥 GRAB IT FAST before it's gone!\n\n" +
                        "Don't miss out on this item. Stock is running low and may sell out soon.\n" +
                        "Visit our inventory system to place your order now.\n\n" +
                        "Best regards,\n" +
                        "Inventory Management Team\n\n" +
                        "---\n" +
                        "You can manage your notification preferences in your account settings."
                );

                try {
                    mailSender.send(message);
                    logger.debug("User alert sent to: {} for product: {}", user.getEmail(), productName);
                } catch (Exception e) {
                    logger.error("Failed to send user alert to {} for {}: {}", user.getEmail(), productName, e.getMessage());
                }
            }
        }
    }
    
    @Async("emailTaskExecutor")
    public void sendStockRestoredNotification(String productName, int newQuantity) {
        logger.info("Sending async stock restored notification for product: {} (quantity: {})", productName, newQuantity);
        
        if (!emailEnabled) {
            logger.info("Email disabled - Stock restored notification would be sent for: {} (quantity: {})", productName, newQuantity);
            return;
        }
        
        try {
            // Notify admin when stock is restored
            SimpleMailMessage adminMessage = new SimpleMailMessage();
            adminMessage.setTo("admin@inventory.com");
            adminMessage.setSubject("✅ Stock Restored - " + productName);

            adminMessage.setText(
                    "STOCK RESTORATION CONFIRMATION\n\n" +
                    "Product: " + productName + "\n" +
                    "New Stock Level: " + newQuantity + " units\n" +
                    "Status: Stock successfully restored\n\n" +
                    "The product is now back to healthy stock levels.\n" +
                    "Users will be able to purchase this item without stock concerns.\n\n" +
                    "Inventory Management System"
            );

            try {
                mailSender.send(adminMessage);
                logger.debug("Stock restored notification sent for: {}", productName);
            } catch (Exception e) {
                logger.error("Failed to send stock restored notification for {}: {}", productName, e.getMessage());
            }
            
            // Notify users who want stock restored alerts
            List<User> allUsers = userRepository.findAll();
            for (User user : allUsers) {
                NotificationPreference preferences = notificationPreferenceRepository.findByUser(user)
                        .orElse(new NotificationPreference(user));
                
                if (preferences.isStockRestoredAlerts()) {
                    SimpleMailMessage userMessage = new SimpleMailMessage();
                    userMessage.setTo(user.getEmail());
                    userMessage.setSubject("✅ Good News! " + productName + " is Back in Stock");

                    userMessage.setText(
                            "Hello " + user.getName() + ",\n\n" +
                            "Great news! The item you were interested in is back in stock!\n\n" +
                            "Product: " + productName + "\n" +
                            "Current Stock: " + newQuantity + " units available\n\n" +
                            "Don't wait - get yours now while supplies last!\n\n" +
                            "Best regards,\n" +
                            "Inventory Management Team"
                    );

                    try {
                        mailSender.send(userMessage);
                        logger.debug("Stock restored alert sent to user: {}", user.getEmail());
                    } catch (Exception e) {
                        logger.error("Failed to send stock restored alert to user {}: {}", user.getEmail(), e.getMessage());
                    }
                }
            }
            
            logger.info("Stock restored notification emails sent successfully for: {}", productName);
        } catch (Exception e) {
            logger.error("Failed to send stock restored notification emails for {}: {}", productName, e.getMessage());
        }
    }
    
    @Async("emailTaskExecutor")
    public void sendWelcomeEmail(User user) {
        logger.info("Sending async welcome email to: {}", user.getEmail());
        
        if (!emailEnabled) {
            logger.info("Email disabled - Welcome email would be sent to: {}", user.getEmail());
            return;
        }
        
        try {
            // Check if user wants welcome emails
            NotificationPreference preferences = notificationPreferenceRepository.findByUser(user)
                    .orElse(new NotificationPreference(user));
            
            if (preferences.isWelcomeEmails()) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(user.getEmail());
                message.setSubject("Welcome to Inventory Management System");

                message.setText(
                        "Hello " + user.getName() + ",\n\n" +
                        "Welcome to our Inventory Management System!\n\n" +
                        "Your account has been successfully created with the following details:\n" +
                        "Email: " + user.getEmail() + "\n" +
                        "Role: " + user.getRole() + "\n\n" +
                        "You can now:\n" +
                        "• Browse available products\n" +
                        "• View inventory reports\n" +
                        "• Receive low stock notifications\n" +
                        "• Manage your notification preferences\n\n" +
                        "Thank you for joining us!\n\n" +
                        "Best regards,\n" +
                        "Inventory Management Team"
                );

                try {
                    mailSender.send(message);
                    logger.debug("Welcome email sent to: {}", user.getEmail());
                } catch (Exception e) {
                    logger.error("Failed to send welcome email to {}: {}", user.getEmail(), e.getMessage());
                }
            } else {
                logger.debug("Welcome email skipped for user {} (disabled in preferences)", user.getEmail());
            }
        } catch (Exception e) {
            logger.error("Failed to process welcome email for {}: {}", user.getEmail(), e.getMessage());
        }
    }
    
    @Async("emailTaskExecutor")
    public void sendPasswordResetEmail(User user, String resetToken) {
        logger.info("Sending async password reset email to: {}", user.getEmail());
        
        String resetUrl = "http://localhost:3001/reset-password?token=" + resetToken;
        
        if (!emailEnabled) {
            // For development - log the reset link to console
            logger.warn("=".repeat(80));
            logger.warn("EMAIL DISABLED - PASSWORD RESET LINK FOR DEVELOPMENT:");
            logger.warn("User: {} ({})", user.getName(), user.getEmail());
            logger.warn("Reset Link: {}", resetUrl);
            logger.warn("=".repeat(80));
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("🔐 Password Reset Request - Inventory Management System");

            message.setText(
                    "Hello " + user.getName() + ",\n\n" +
                    "You have requested to reset your password for the Inventory Management System.\n\n" +
                    "Click the link below to reset your password:\n" +
                    resetUrl + "\n\n" +
                    "This link will expire in 1 hour for security reasons.\n\n" +
                    "If you did not request this password reset, please ignore this email.\n" +
                    "Your password will remain unchanged.\n\n" +
                    "For security reasons, please do not share this link with anyone.\n\n" +
                    "Best regards,\n" +
                    "Inventory Management Team\n\n" +
                    "---\n" +
                    "If you're having trouble clicking the link, copy and paste it into your browser."
            );

            try {
                mailSender.send(message);
                logger.debug("Password reset email sent to: {}", user.getEmail());
            } catch (Exception e) {
                logger.error("Failed to send password reset email to {}: {}", user.getEmail(), e.getMessage());
            }
        } catch (Exception e) {
            logger.error("Failed to process password reset email for {}: {}", user.getEmail(), e.getMessage());
        }
    }

    @Async("emailTaskExecutor")
        public void sendOTPEmail(String toEmail, String userName, String otp) {
            logger.info("Sending async OTP email to: {}", toEmail);

            if (!emailEnabled) {
                logger.warn("=".repeat(80));
                logger.warn("EMAIL DISABLED - OTP FOR DEVELOPMENT:");
                logger.warn("User: {} ({})", userName, toEmail);
                logger.warn("OTP Code: {}", otp);
                logger.warn("Valid for: 10 minutes");
                logger.warn("=".repeat(80));

                // In development mode, we still consider the email "sent" successfully
                // so the user can use the OTP from the console logs
                return;
            }

            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(toEmail);
                message.setSubject("🔐 Password Reset OTP - Inventory Management System");

                message.setText(
                        "Hello " + userName + ",\n\n" +
                        "You have requested to reset your password for the Inventory Management System.\n\n" +
                        "Your One-Time Password (OTP) is:\n\n" +
                        "🔢 " + otp + "\n\n" +
                        "⏰ This OTP is valid for 10 minutes only.\n\n" +
                        "SECURITY NOTICE:\n" +
                        "• Never share this OTP with anyone\n" +
                        "• We will never ask for your OTP via phone or email\n" +
                        "• If you didn't request this reset, please ignore this email\n\n" +
                        "Enter this OTP on the password reset page to continue.\n\n" +
                        "Best regards,\n" +
                        "Inventory Management Team\n\n" +
                        "---\n" +
                        "This is an automated message, please do not reply to this email."
                );

                try {
                    mailSender.send(message);
                    logger.debug("OTP email sent to: {}", toEmail);
                } catch (Exception e) {
                    logger.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
                    throw new RuntimeException("Failed to send OTP email", e);
                }
            } catch (Exception e) {
                logger.error("Failed to process OTP email for {}: {}", toEmail, e.getMessage());
                throw new RuntimeException("Failed to send OTP email", e);
            }
        }


    @Async("emailTaskExecutor")
    public void sendPasswordResetConfirmationEmail(User user) {
        logger.info("Sending async password reset confirmation email to: {}", user.getEmail());
        
        if (!emailEnabled) {
            logger.info("Email disabled - Password reset confirmation would be sent to: {}", user.getEmail());
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("✅ Password Reset Successful - Inventory Management System");

            message.setText(
                    "Hello " + user.getName() + ",\n\n" +
                    "Your password has been successfully reset for the Inventory Management System.\n\n" +
                    "If you did not make this change, please contact our support team immediately.\n\n" +
                    "For your security:\n" +
                    "• Use a strong, unique password\n" +
                    "• Don't share your login credentials\n" +
                    "• Log out when using shared computers\n\n" +
                    "You can now log in with your new password.\n\n" +
                    "Best regards,\n" +
                    "Inventory Management Team"
            );

            try {
                mailSender.send(message);
                logger.debug("Password reset confirmation sent to: {}", user.getEmail());
            } catch (Exception e) {
                logger.error("Failed to send password reset confirmation to {}: {}", user.getEmail(), e.getMessage());
            }
        } catch (Exception e) {
            logger.error("Failed to process password reset confirmation for {}: {}", user.getEmail(), e.getMessage());
        }
    }
}