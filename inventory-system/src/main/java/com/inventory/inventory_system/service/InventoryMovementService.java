package com.inventory.inventory_system.service;

import com.inventory.inventory_system.entity.InventoryMovement;
import com.inventory.inventory_system.entity.Product;
import com.inventory.inventory_system.repository.InventoryMovementRepository;
import com.inventory.inventory_system.repository.ProductRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class InventoryMovementService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryMovementService.class);

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;
    
    @Autowired
    private ProductRepository productRepository;

    // Record inventory movement
    public InventoryMovement recordMovement(Long productId, String movementType, 
                                          int quantityBefore, int quantityChanged, 
                                          int quantityAfter, String reason, 
                                          String userEmail, String notes) {
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            InventoryMovement movement = new InventoryMovement(
                product, movementType, quantityBefore, quantityChanged, 
                quantityAfter, reason, userEmail, notes
            );

            InventoryMovement saved = inventoryMovementRepository.save(movement);
            logger.info("Inventory movement recorded: {} {} units for product {}", 
                       movementType, quantityChanged, product.getName());
            
            return saved;
        } catch (Exception e) {
            logger.error("Failed to record inventory movement: {}", e.getMessage());
            throw new RuntimeException("Failed to record inventory movement: " + e.getMessage());
        }
    }

    // Get all movements (last 50)
    @Transactional(readOnly = true)
    public List<InventoryMovement> getAllMovements() {
        return inventoryMovementRepository.findTop50ByOrderByTimestampDesc();
    }

    // Get movements by product
    @Transactional(readOnly = true)
    public List<InventoryMovement> getMovementsByProduct(Long productId) {
        return inventoryMovementRepository.findByProductIdOrderByTimestampDesc(productId);
    }

    // Get movements by type
    @Transactional(readOnly = true)
    public List<InventoryMovement> getMovementsByType(String movementType) {
        return inventoryMovementRepository.findByMovementTypeOrderByTimestampDesc(movementType);
    }

    // Get movements by user
    @Transactional(readOnly = true)
    public List<InventoryMovement> getMovementsByUser(String userEmail) {
        return inventoryMovementRepository.findByUserEmailOrderByTimestampDesc(userEmail);
    }

    // Get movements by date range
    @Transactional(readOnly = true)
    public List<InventoryMovement> getMovementsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return inventoryMovementRepository.findByTimestampBetween(startDate, endDate);
    }

    // Get movements by product and date range
    @Transactional(readOnly = true)
    public List<InventoryMovement> getMovementsByProductAndDateRange(Long productId, 
                                                                    LocalDateTime startDate, 
                                                                    LocalDateTime endDate) {
        return inventoryMovementRepository.findByProductAndTimestampBetween(productId, startDate, endDate);
    }

    // Convenience methods for common movement types
    public InventoryMovement recordStockIncrease(Long productId, int quantityBefore, 
                                               int quantityAdded, String reason, 
                                               String userEmail, String notes) {
        return recordMovement(productId, "IN", quantityBefore, quantityAdded, 
                            quantityBefore + quantityAdded, reason, userEmail, notes);
    }

    public InventoryMovement recordStockDecrease(Long productId, int quantityBefore, 
                                               int quantityRemoved, String reason, 
                                               String userEmail, String notes) {
        return recordMovement(productId, "OUT", quantityBefore, quantityRemoved, 
                            quantityBefore - quantityRemoved, reason, userEmail, notes);
    }

    public InventoryMovement recordStockAdjustment(Long productId, int quantityBefore, 
                                                 int quantityAfter, String reason, 
                                                 String userEmail, String notes) {
        int quantityChanged = quantityAfter - quantityBefore;
        return recordMovement(productId, "ADJUSTMENT", quantityBefore, quantityChanged, 
                            quantityAfter, reason, userEmail, notes);
    }
}