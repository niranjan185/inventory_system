package com.inventory.inventory_system.service;

import com.inventory.inventory_system.dto.LowStockLogDTO;
import com.inventory.inventory_system.entity.LowStockLog;
import com.inventory.inventory_system.entity.Product;
import com.inventory.inventory_system.repository.LowStockLogRepository;
import com.inventory.inventory_system.util.LowStockLogMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LowStockLogService {

    private static final Logger logger = LoggerFactory.getLogger(LowStockLogService.class);

    @Autowired
    private LowStockLogRepository lowStockLogRepository;

    // Create or update low stock log for a product
    public LowStockLog logLowStock(Product product) {
        logger.info("Logging low stock for product: {} (ID: {})", product.getName(), product.getId());
        
        // Check if there's already an active log for this product
        Optional<LowStockLog> existingLog = lowStockLogRepository.findByProductIdAndIsResolvedFalse(product.getId());
        
        String status = determineStockStatus(product.getQuantity(), product.getReorderLevel());
        
        if (existingLog.isPresent()) {
            // Update existing log
            LowStockLog log = existingLog.get();
            log.setCurrentQuantity(product.getQuantity());
            log.setStatus(status);
            log.setPrice(product.getPrice());
            logger.info("Updated existing low stock log for product: {}", product.getName());
            return lowStockLogRepository.save(log);
        } else {
            // Create new log
            LowStockLog newLog = new LowStockLog(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getQuantity(),
                product.getReorderLevel(),
                product.getPrice(),
                status
            );
            logger.info("Created new low stock log for product: {}", product.getName());
            return lowStockLogRepository.save(newLog);
        }
    }

    // Resolve low stock log when stock is restored
    public void resolveLowStock(Long productId, String resolvedBy, String notes) {
        logger.info("Resolving low stock for product ID: {} by user: {}", productId, resolvedBy);
        
        Optional<LowStockLog> activeLog = lowStockLogRepository.findByProductIdAndIsResolvedFalse(productId);
        if (activeLog.isPresent()) {
            LowStockLog log = activeLog.get();
            log.setResolved(true);
            log.setResolvedBy(resolvedBy);
            log.setNotes(notes);
            log.setResolvedAt(LocalDateTime.now());
            lowStockLogRepository.save(log);
            logger.info("Resolved low stock log for product ID: {}", productId);
        }
    }

    // Get all unresolved low stock logs
    @Transactional(readOnly = true)
    public List<LowStockLogDTO> getUnresolvedLogs() {
        List<LowStockLog> logs = lowStockLogRepository.findByIsResolvedFalseOrderByDetectedAtDesc();
        return LowStockLogMapper.toDTOList(logs);
    }

    // Get all logs for a specific product
    @Transactional(readOnly = true)
    public List<LowStockLogDTO> getLogsByProduct(Long productId) {
        List<LowStockLog> logs = lowStockLogRepository.findByProductIdOrderByDetectedAtDesc(productId);
        return LowStockLogMapper.toDTOList(logs);
    }

    // Get logs by date range
    @Transactional(readOnly = true)
    public List<LowStockLogDTO> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<LowStockLog> logs = lowStockLogRepository.findByDateRange(startDate, endDate);
        return LowStockLogMapper.toDTOList(logs);
    }

    // Get logs by status
    @Transactional(readOnly = true)
    public List<LowStockLogDTO> getLogsByStatus(String status) {
        List<LowStockLog> logs = lowStockLogRepository.findByStatusOrderByDetectedAtDesc(status);
        return LowStockLogMapper.toDTOList(logs);
    }

    // Get logs by category
    @Transactional(readOnly = true)
    public List<LowStockLogDTO> getLogsByCategory(String category) {
        List<LowStockLog> logs = lowStockLogRepository.findByCategoryOrderByDetectedAtDesc(category);
        return LowStockLogMapper.toDTOList(logs);
    }

    // Get recent logs (last 30 days)
    @Transactional(readOnly = true)
    public List<LowStockLogDTO> getRecentLogs() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<LowStockLog> logs = lowStockLogRepository.findRecentLogs(thirtyDaysAgo);
        return LowStockLogMapper.toDTOList(logs);
    }

    // Get all logs with pagination support
    @Transactional(readOnly = true)
    public List<LowStockLogDTO> getAllLogs() {
        List<LowStockLog> logs = lowStockLogRepository.findAll();
        return LowStockLogMapper.toDTOList(logs);
    }

    // Get statistics
    @Transactional(readOnly = true)
    public LowStockStatistics getStatistics() {
        long totalUnresolved = lowStockLogRepository.countByIsResolvedFalse();
        long criticalCount = lowStockLogRepository.countByStatus("OUT_OF_STOCK");
        long lowStockCount = lowStockLogRepository.countByStatus("LOW_STOCK");
        
        return new LowStockStatistics(totalUnresolved, criticalCount, lowStockCount);
    }

    // Generate CSV for low stock logs
    public String generateLowStockLogsCsv() {
        List<LowStockLogDTO> logs = getAllLogs();
        StringBuilder csv = new StringBuilder();
        
        // CSV Header
        csv.append("ID,Product Name,Category,Current Quantity,Reorder Level,Price,Status,Detected At,Resolved At,Resolved By,Days Unresolved,Severity,Total Value,Notes\n");
        
        // CSV Data
        for (LowStockLogDTO log : logs) {
            csv.append(log.getId()).append(",")
               .append("\"").append(log.getProductName()).append("\",")
               .append("\"").append(log.getCategory()).append("\",")
               .append(log.getCurrentQuantity()).append(",")
               .append(log.getReorderLevel()).append(",")
               .append(log.getPrice()).append(",")
               .append(log.getStatus()).append(",")
               .append(log.getDetectedAt()).append(",")
               .append(log.getResolvedAt() != null ? log.getResolvedAt() : "").append(",")
               .append(log.getResolvedBy() != null ? "\"" + log.getResolvedBy() + "\"" : "").append(",")
               .append(log.getDaysUnresolved()).append(",")
               .append(log.getSeverity()).append(",")
               .append(log.getTotalValue()).append(",")
               .append(log.getNotes() != null ? "\"" + log.getNotes() + "\"" : "").append("\n");
        }
        
        return csv.toString();
    }

    private String determineStockStatus(int quantity, Integer reorderLevel) {
        if (quantity == 0) return "OUT_OF_STOCK";
        if (reorderLevel != null && quantity <= reorderLevel) return "LOW_STOCK";
        return "IN_STOCK";
    }

    // Inner class for statistics
    public static class LowStockStatistics {
        private final long totalUnresolved;
        private final long criticalCount;
        private final long lowStockCount;

        public LowStockStatistics(long totalUnresolved, long criticalCount, long lowStockCount) {
            this.totalUnresolved = totalUnresolved;
            this.criticalCount = criticalCount;
            this.lowStockCount = lowStockCount;
        }

        public long getTotalUnresolved() { return totalUnresolved; }
        public long getCriticalCount() { return criticalCount; }
        public long getLowStockCount() { return lowStockCount; }
    }
}