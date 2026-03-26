package com.inventory.inventory_system.service;

import com.inventory.inventory_system.entity.AuditLog;
import com.inventory.inventory_system.repository.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AuditLogService {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogService.class);

    @Autowired
    private AuditLogRepository auditLogRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Log audit entry
    public void logAudit(String entityType, Long entityId, String action, String userEmail, 
                        Object oldValues, Object newValues, String ipAddress) {
        try {
            String oldValuesJson = oldValues != null ? objectMapper.writeValueAsString(oldValues) : null;
            String newValuesJson = newValues != null ? objectMapper.writeValueAsString(newValues) : null;

            AuditLog auditLog = new AuditLog(
                entityType, entityId, action, userEmail, 
                oldValuesJson, newValuesJson, ipAddress
            );

            auditLogRepository.save(auditLog);
            logger.info("Audit log created: {} {} by {}", action, entityType, userEmail);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize audit data: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to create audit log: {}", e.getMessage());
        }
    }

    // Get all audit logs (paginated)
    @Transactional(readOnly = true)
    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findTop50ByOrderByTimestampDesc();
    }

    // Get audit logs by entity type
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsByEntityType(String entityType) {
        return auditLogRepository.findByEntityTypeOrderByTimestampDesc(entityType);
    }

    // Get audit logs by entity ID and type
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsByEntity(Long entityId, String entityType) {
        return auditLogRepository.findByEntityIdAndEntityTypeOrderByTimestampDesc(entityId, entityType);
    }

    // Get audit logs by user
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsByUser(String userEmail) {
        return auditLogRepository.findByUserEmailOrderByTimestampDesc(userEmail);
    }

    // Get audit logs by date range
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByTimestampBetween(startDate, endDate);
    }

    // Convenience methods for common operations
    public void logProductCreate(Long productId, String userEmail, Object productData, String ipAddress) {
        logAudit("Product", productId, "CREATE", userEmail, null, productData, ipAddress);
    }

    public void logProductUpdate(Long productId, String userEmail, Object oldData, Object newData, String ipAddress) {
        logAudit("Product", productId, "UPDATE", userEmail, oldData, newData, ipAddress);
    }

    public void logProductDelete(Long productId, String userEmail, Object productData, String ipAddress) {
        logAudit("Product", productId, "DELETE", userEmail, productData, null, ipAddress);
    }

    public void logUserCreate(Long userId, String userEmail, Object userData, String ipAddress) {
        logAudit("User", userId, "CREATE", userEmail, null, userData, ipAddress);
    }

    public void logUserUpdate(Long userId, String userEmail, Object oldData, Object newData, String ipAddress) {
        logAudit("User", userId, "UPDATE", userEmail, oldData, newData, ipAddress);
    }

    public void logUserDelete(Long userId, String userEmail, Object userData, String ipAddress) {
        logAudit("User", userId, "DELETE", userEmail, userData, null, ipAddress);
    }

    public void logLogin(String userEmail, String ipAddress) {
        logAudit("User", null, "LOGIN", userEmail, null, null, ipAddress);
    }

    public void logLogout(String userEmail, String ipAddress) {
        logAudit("User", null, "LOGOUT", userEmail, null, null, ipAddress);
    }
}