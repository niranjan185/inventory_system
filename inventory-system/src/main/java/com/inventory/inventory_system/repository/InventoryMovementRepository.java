package com.inventory.inventory_system.repository;

import com.inventory.inventory_system.entity.InventoryMovement;
import com.inventory.inventory_system.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {
    
    List<InventoryMovement> findByProductOrderByTimestampDesc(Product product);
    
    List<InventoryMovement> findByProductIdOrderByTimestampDesc(Long productId);
    
    List<InventoryMovement> findByMovementTypeOrderByTimestampDesc(String movementType);
    
    List<InventoryMovement> findByUserEmailOrderByTimestampDesc(String userEmail);
    
    @Query("SELECT im FROM InventoryMovement im WHERE im.timestamp BETWEEN :startDate AND :endDate ORDER BY im.timestamp DESC")
    List<InventoryMovement> findByTimestampBetween(@Param("startDate") LocalDateTime startDate, 
                                                  @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT im FROM InventoryMovement im WHERE im.product.id = :productId AND im.timestamp BETWEEN :startDate AND :endDate ORDER BY im.timestamp DESC")
    List<InventoryMovement> findByProductAndTimestampBetween(@Param("productId") Long productId,
                                                            @Param("startDate") LocalDateTime startDate, 
                                                            @Param("endDate") LocalDateTime endDate);
    
    List<InventoryMovement> findTop50ByOrderByTimestampDesc();
}