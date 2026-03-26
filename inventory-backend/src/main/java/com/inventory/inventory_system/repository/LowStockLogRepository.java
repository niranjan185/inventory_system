package com.inventory.inventory_system.repository;

import com.inventory.inventory_system.entity.LowStockLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LowStockLogRepository extends JpaRepository<LowStockLog, Long> {
    
    // Find all unresolved low stock logs
    List<LowStockLog> findByIsResolvedFalseOrderByDetectedAtDesc();
    
    // Find all logs for a specific product
    List<LowStockLog> findByProductIdOrderByDetectedAtDesc(Long productId);
    
    // Find active (unresolved) log for a specific product
    Optional<LowStockLog> findByProductIdAndIsResolvedFalse(Long productId);
    
    // Find logs by date range
    @Query("SELECT l FROM LowStockLog l WHERE l.detectedAt BETWEEN :startDate AND :endDate ORDER BY l.detectedAt DESC")
    List<LowStockLog> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);
    
    // Find logs by status
    List<LowStockLog> findByStatusOrderByDetectedAtDesc(String status);
    
    // Find logs by category
    List<LowStockLog> findByCategoryOrderByDetectedAtDesc(String category);
    
    // Count unresolved logs
    long countByIsResolvedFalse();
    
    // Count logs by status
    long countByStatus(String status);
    
    // Find recent logs (last 30 days)
    @Query("SELECT l FROM LowStockLog l WHERE l.detectedAt >= :thirtyDaysAgo ORDER BY l.detectedAt DESC")
    List<LowStockLog> findRecentLogs(@Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);
}