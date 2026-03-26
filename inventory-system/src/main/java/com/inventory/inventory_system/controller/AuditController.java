package com.inventory.inventory_system.controller;

import com.inventory.inventory_system.entity.AuditLog;
import com.inventory.inventory_system.service.AuditLogService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    @Autowired
    private AuditLogService auditLogService;

    // Get all audit logs (last 50)
    @GetMapping
    public List<AuditLog> getAllAuditLogs() {
        return auditLogService.getAllAuditLogs();
    }

    // Get audit logs by entity type
    @GetMapping("/entity-type/{entityType}")
    public List<AuditLog> getAuditLogsByEntityType(@PathVariable String entityType) {
        return auditLogService.getAuditLogsByEntityType(entityType);
    }

    // Get audit logs for specific entity
    @GetMapping("/entity/{entityType}/{entityId}")
    public List<AuditLog> getAuditLogsByEntity(@PathVariable String entityType, 
                                              @PathVariable Long entityId) {
        return auditLogService.getAuditLogsByEntity(entityId, entityType);
    }

    // Get audit logs by user
    @GetMapping("/user/{userEmail}")
    public List<AuditLog> getAuditLogsByUser(@PathVariable String userEmail) {
        return auditLogService.getAuditLogsByUser(userEmail);
    }

    // Get audit logs by date range
    @GetMapping("/date-range")
    public ResponseEntity<List<AuditLog>> getAuditLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().build();
        }
        
        List<AuditLog> logs = auditLogService.getAuditLogsByDateRange(startDate, endDate);
        return ResponseEntity.ok(logs);
    }
}