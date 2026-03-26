package com.inventory.inventory_system.service;

import com.inventory.inventory_system.entity.Product;
import com.inventory.inventory_system.repository.ProductRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private AlertService alertService;
    
    @Autowired
    private AuditLogService auditLogService;
    
    @Autowired
    private InventoryMovementService inventoryMovementService;
    
    @Autowired
    private LowStockLogService lowStockLogService;

    // Add product with audit logging
    @Transactional
    public Product addProduct(Product product, String userEmail, String ipAddress) {
        Product savedProduct = productRepository.save(product);
        
        // Log audit
        auditLogService.logProductCreate(savedProduct.getId(), userEmail, savedProduct, ipAddress);
        
        // Record initial inventory movement
        if (savedProduct.getQuantity() > 0) {
            inventoryMovementService.recordStockIncrease(
                savedProduct.getId(), 0, savedProduct.getQuantity(), 
                "INITIAL_STOCK", userEmail, "Initial stock entry"
            );
        }
        
        return savedProduct;
    }

    // Backward compatibility method
    public Product addProduct(Product product) {
        return addProduct(product, "system", "127.0.0.1");
    }

    // Get all products
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        logger.debug("Retrieving all products");
        return productRepository.findAll();
    }

    // Get product by ID
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    // Update product with audit and movement tracking
    @Transactional
    public Product updateProduct(Long id, Product updatedProduct, String userEmail, String ipAddress) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));

        // Store old values for audit
        Product oldProduct = new Product(
            existingProduct.getId(), existingProduct.getName(), existingProduct.getCategory(),
            existingProduct.getPrice(), existingProduct.getQuantity(), existingProduct.getReorderLevel()
        );

        // Store previous quantity to check for stock restoration
        int previousQuantity = existingProduct.getQuantity();
        Integer currentReorderLevel = existingProduct.getReorderLevel();
        boolean wasLowStock = currentReorderLevel != null && previousQuantity <= currentReorderLevel;

        // Update product fields
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setCategory(updatedProduct.getCategory());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setQuantity(updatedProduct.getQuantity());
        existingProduct.setReorderLevel(updatedProduct.getReorderLevel());

        Product savedProduct = productRepository.save(existingProduct);

        // Log audit
        auditLogService.logProductUpdate(savedProduct.getId(), userEmail, oldProduct, savedProduct, ipAddress);

        // Record inventory movement if quantity changed
        if (previousQuantity != savedProduct.getQuantity()) {
            String reason = savedProduct.getQuantity() > previousQuantity ? "STOCK_INCREASE" : "STOCK_DECREASE";
            inventoryMovementService.recordStockAdjustment(
                savedProduct.getId(), previousQuantity, savedProduct.getQuantity(),
                reason, userEmail, "Product update - quantity changed"
            );
        }

        // Check for low stock alert
        Integer reorderLevel = savedProduct.getReorderLevel();
        if (reorderLevel != null && savedProduct.getQuantity() <= reorderLevel) {
            // Log low stock event
            lowStockLogService.logLowStock(savedProduct);
            
            // Create alert in database
            alertService.createLowStockAlert(
                    savedProduct.getName(),
                    savedProduct.getQuantity()
            );
            
            // Send email notifications asynchronously (non-blocking)
            emailService.sendLowStockAlert(
                    savedProduct.getName(),
                    savedProduct.getQuantity()
            );
        } 
        // Check for stock restoration (was low stock, now above reorder level)
        else if (wasLowStock && reorderLevel != null && savedProduct.getQuantity() > reorderLevel) {
            // Resolve low stock log
            lowStockLogService.resolveLowStock(savedProduct.getId(), userEmail, "Stock restored through product update");
            
            // Send email notifications asynchronously (non-blocking)
            emailService.sendStockRestoredNotification(
                    savedProduct.getName(),
                    savedProduct.getQuantity()
            );
        }

        return savedProduct;
    }

    // Backward compatibility method
    public Product updateProduct(Long id, Product updatedProduct) {
        return updateProduct(id, updatedProduct, "system", "127.0.0.1");
    }

    // Delete product with audit logging
    @Transactional
    public void deleteProduct(Long id, String userEmail, String ipAddress) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));

        // Log audit before deletion
        auditLogService.logProductDelete(id, userEmail, product, ipAddress);

        // Record inventory movement for stock removal
        if (product.getQuantity() > 0) {
            inventoryMovementService.recordStockDecrease(
                id, product.getQuantity(), product.getQuantity(),
                "PRODUCT_DELETED", userEmail, "Product deleted from system"
            );
        }

        productRepository.deleteById(id);
    }

    // Backward compatibility method
    public void deleteProduct(Long id) {
        deleteProduct(id, "system", "127.0.0.1");
    }
    
    // Get products by category
    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(String category) {
        logger.debug("Retrieving products by category: {}", category);
        return productRepository.findByCategoryContainingIgnoreCase(category);
    }
    
    // Get low stock products
    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }
    
    // Bulk update stock with audit and movement tracking
    @Transactional
    public List<Product> bulkUpdateStock(List<Product> products, String userEmail, String ipAddress) {
        List<Product> updatedProducts = new java.util.ArrayList<>();
        
        for (Product productUpdate : products) {
            Product updated = updateProduct(productUpdate.getId(), productUpdate, userEmail, ipAddress);
            if (updated != null) {
                updatedProducts.add(updated);
            }
        }
        
        return updatedProducts;
    }

    // Backward compatibility method
    public List<Product> bulkUpdateStock(List<Product> products) {
        return bulkUpdateStock(products, "system", "127.0.0.1");
    }
}