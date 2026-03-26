package com.inventory.inventory_system.controller;

import com.inventory.inventory_system.dto.LowStockLogDTO;
import com.inventory.inventory_system.service.LowStockLogService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/low-stock-logs")
@CrossOrigin(origins = "*")
public class LowStockLogController {

    @Autowired
    private LowStockLogService lowStockLogService;

    // Get all unresolved low stock logs
    @GetMapping("/unresolved")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<LowStockLogDTO>> getUnresolvedLogs() {
        try {
            List<LowStockLogDTO> logs = lowStockLogService.getUnresolvedLogs();
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get all logs
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<LowStockLogDTO>> getAllLogs() {
        try {
            List<LowStockLogDTO> logs = lowStockLogService.getAllLogs();
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get logs by product ID
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<LowStockLogDTO>> getLogsByProduct(@PathVariable Long productId) {
        try {
            List<LowStockLogDTO> logs = lowStockLogService.getLogsByProduct(productId);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get logs by date range
    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<LowStockLogDTO>> getLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            List<LowStockLogDTO> logs = lowStockLogService.getLogsByDateRange(startDate, endDate);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get logs by status
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<LowStockLogDTO>> getLogsByStatus(@PathVariable String status) {
        try {
            List<LowStockLogDTO> logs = lowStockLogService.getLogsByStatus(status);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get logs by category
    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<LowStockLogDTO>> getLogsByCategory(@PathVariable String category) {
        try {
            List<LowStockLogDTO> logs = lowStockLogService.getLogsByCategory(category);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get recent logs (last 30 days)
    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<LowStockLogDTO>> getRecentLogs() {
        try {
            List<LowStockLogDTO> logs = lowStockLogService.getRecentLogs();
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get low stock statistics
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<LowStockLogService.LowStockStatistics> getStatistics() {
        try {
            LowStockLogService.LowStockStatistics stats = lowStockLogService.getStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Resolve low stock log (Admin only)
    @PutMapping("/resolve/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> resolveLowStock(
            @PathVariable Long productId,
            @RequestParam(required = false) String notes,
            Authentication authentication) {
        try {
            String resolvedBy = authentication.getName();
            lowStockLogService.resolveLowStock(productId, resolvedBy, notes);
            return ResponseEntity.ok("Low stock log resolved successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error resolving low stock log: " + e.getMessage());
        }
    }

    // Export low stock logs to CSV
    @GetMapping("/export/csv")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<String> exportLowStockLogsCsv() {
        try {
            String csvData = lowStockLogService.generateLowStockLogsCsv();
            
            String filename = "low_stock_logs_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", filename);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error generating CSV: " + e.getMessage());
        }
    }
}