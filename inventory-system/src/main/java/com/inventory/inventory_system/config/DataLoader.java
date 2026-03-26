package com.inventory.inventory_system.config;

import com.inventory.inventory_system.entity.Product;
import com.inventory.inventory_system.entity.User;
import com.inventory.inventory_system.repository.ProductRepository;
import com.inventory.inventory_system.repository.UserRepository;
import com.inventory.inventory_system.repository.NotificationPreferenceRepository;
import com.inventory.inventory_system.repository.LowStockLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private NotificationPreferenceRepository notificationPreferenceRepository;
    
    @Autowired
    private LowStockLogRepository lowStockLogRepository;

    @Override
    public void run(String... args) throws Exception {
        // Clean up any corrupted data first
        cleanupCorruptedData();
        
        // Create admin user if not exists
        if (userRepository.findByEmail("admin@inventory.com").isEmpty()) {
            User admin = new User();
            admin.setName("Admin User");
            admin.setEmail("admin@inventory.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            userRepository.save(admin);
            System.out.println("Admin user created: admin@inventory.com / admin123");
        }
        
        // Create sample user if not exists
        if (userRepository.findByEmail("user@inventory.com").isEmpty()) {
            User user = new User();
            user.setName("Regular User");
            user.setEmail("user@inventory.com");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setRole("USER");
            userRepository.save(user);
            System.out.println("Sample user created: user@inventory.com / user123");
        } else {
            // Update existing user password to ensure correct encoding
            User existingUser = userRepository.findByEmail("user@inventory.com").get();
            existingUser.setPassword(passwordEncoder.encode("user123"));
            userRepository.save(existingUser);
            System.out.println("Sample user password updated: user@inventory.com / user123");
        }
        
        // Clean up orphaned notification preferences
        cleanupNotificationPreferences();
        
        // Clean up orphaned low stock logs
        cleanupLowStockLogs();
        
        // Create sample products if database is empty
        if (productRepository.count() == 0) {
            createSampleProducts();
        }
        
        System.out.println("Data initialization completed!");
    }
    
    private void cleanupCorruptedData() {
        try {
            // Find and fix any products with null reorderLevel
            List<Product> corruptedProducts = productRepository.findAll().stream()
                .filter(p -> p.getReorderLevel() == null)
                .toList();
            
            if (!corruptedProducts.isEmpty()) {
                System.out.println("Found " + corruptedProducts.size() + " products with null reorderLevel. Fixing...");
                for (Product product : corruptedProducts) {
                    product.setReorderLevel(0); // Set default value
                    productRepository.save(product);
                }
                System.out.println("Fixed " + corruptedProducts.size() + " corrupted products");
            }
            
            // Delete all existing products to start fresh if there are still issues
            long productCount = productRepository.count();
            if (productCount > 0) {
                System.out.println("Cleaning up existing products to prevent data corruption issues...");
                productRepository.deleteAll();
                System.out.println("Cleaned up " + productCount + " existing products");
            }
        } catch (Exception e) {
            System.err.println("Error during cleanup: " + e.getMessage());
            // If cleanup fails, try to delete all products
            try {
                productRepository.deleteAll();
                System.out.println("Performed emergency cleanup - deleted all products");
            } catch (Exception ex) {
                System.err.println("Emergency cleanup also failed: " + ex.getMessage());
            }
        }
    }
    
    private void createSampleProducts() {
        List<Product> sampleProducts = Arrays.asList(
            new Product(null, "Laptop", "Electronics", 999.99, 25, 5),
            new Product(null, "Mouse", "Electronics", 29.99, 50, 10),
            new Product(null, "Keyboard", "Electronics", 79.99, 30, 8),
            new Product(null, "Monitor", "Electronics", 299.99, 15, 3),
            new Product(null, "Desk Chair", "Furniture", 199.99, 12, 5),
            new Product(null, "Office Desk", "Furniture", 349.99, 8, 2),
            new Product(null, "Notebook", "Stationery", 4.99, 100, 20),
            new Product(null, "Pen Set", "Stationery", 12.99, 75, 15),
            new Product(null, "Printer Paper", "Stationery", 19.99, 40, 10),
            new Product(null, "Coffee Maker", "Appliances", 89.99, 6, 3),
            new Product(null, "Water Bottle", "Accessories", 14.99, 35, 8),
            new Product(null, "Phone Case", "Accessories", 24.99, 60, 12),
            // Low stock items for testing alerts
            new Product(null, "Tablet", "Electronics", 399.99, 2, 5),
            new Product(null, "Headphones", "Electronics", 149.99, 1, 4),
            new Product(null, "USB Cable", "Electronics", 9.99, 3, 10)
        );
        
        productRepository.saveAll(sampleProducts);
        System.out.println("Sample products created: " + sampleProducts.size() + " products");
    }
    
    private void cleanupNotificationPreferences() {
        try {
            // Find orphaned notification preferences (preferences without valid users)
            var allPreferences = notificationPreferenceRepository.findAll();
            var allUsers = userRepository.findAll();
            var validUserIds = allUsers.stream().map(User::getId).collect(java.util.stream.Collectors.toSet());
            
            int orphanedCount = 0;
            for (var pref : allPreferences) {
                if (pref.getUser() == null || !validUserIds.contains(pref.getUser().getId())) {
                    System.out.println("Removing orphaned notification preference with ID: " + pref.getId());
                    notificationPreferenceRepository.delete(pref);
                    orphanedCount++;
                }
            }
            
            if (orphanedCount > 0) {
                System.out.println("Cleaned up " + orphanedCount + " orphaned notification preferences");
            }
            
            // Check for duplicate preferences for the same user
            var userPreferenceCounts = new java.util.HashMap<Long, Integer>();
            for (var pref : notificationPreferenceRepository.findAll()) {
                if (pref.getUser() != null) {
                    Long userId = pref.getUser().getId();
                    userPreferenceCounts.put(userId, userPreferenceCounts.getOrDefault(userId, 0) + 1);
                }
            }
            
            int duplicatesRemoved = 0;
            for (var entry : userPreferenceCounts.entrySet()) {
                if (entry.getValue() > 1) {
                    Long userId = entry.getKey();
                    System.out.println("Found " + entry.getValue() + " notification preferences for user ID: " + userId + ". Keeping only the first one.");
                    
                    var duplicatePrefs = notificationPreferenceRepository.findAll().stream()
                        .filter(p -> p.getUser() != null && p.getUser().getId().equals(userId))
                        .skip(1) // Keep the first one, remove the rest
                        .collect(java.util.stream.Collectors.toList());
                    
                    for (var dupPref : duplicatePrefs) {
                        notificationPreferenceRepository.delete(dupPref);
                        duplicatesRemoved++;
                    }
                }
            }
            
            if (duplicatesRemoved > 0) {
                System.out.println("Removed " + duplicatesRemoved + " duplicate notification preferences");
            }
            
        } catch (Exception e) {
            System.err.println("Error during notification preferences cleanup: " + e.getMessage());
        }
    }
    
    private void cleanupLowStockLogs() {
        try {
            // Find and remove orphaned low stock logs (logs for products that no longer exist)
            var allLogs = lowStockLogRepository.findAll();
            var allProducts = productRepository.findAll();
            var validProductIds = allProducts.stream().map(Product::getId).collect(java.util.stream.Collectors.toSet());
            
            int orphanedCount = 0;
            for (var log : allLogs) {
                if (!validProductIds.contains(log.getProductId())) {
                    System.out.println("Removing orphaned low stock log for product ID: " + log.getProductId());
                    lowStockLogRepository.delete(log);
                    orphanedCount++;
                }
            }
            
            if (orphanedCount > 0) {
                System.out.println("Cleaned up " + orphanedCount + " orphaned low stock logs");
            }
            
        } catch (Exception e) {
            System.err.println("Error during low stock logs cleanup: " + e.getMessage());
        }
    }
}