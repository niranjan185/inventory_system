package com.inventory.inventory_system.controller;

import com.inventory.inventory_system.dto.InventoryReport;
import com.inventory.inventory_system.service.ReportService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private ReportService reportService;

    // Generate inventory summary report
    @GetMapping("/inventory")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getInventoryReport(Authentication authentication) {
        try {
            System.out.println("=== INVENTORY REPORT REQUEST DEBUG ===");
            System.out.println("Authentication: " + authentication);
            System.out.println("Principal: " + (authentication != null ? authentication.getName() : "null"));
            System.out.println("Authorities: " + (authentication != null ? authentication.getAuthorities() : "null"));
            System.out.println("Is authenticated: " + (authentication != null ? authentication.isAuthenticated() : "false"));
            
            InventoryReport report = reportService.generateInventoryReport();
            System.out.println("Report generated successfully");
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            System.err.println("Error generating inventory report: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error generating inventory report: " + e.getMessage());
        }
    }

    // Export all products to CSV
    @GetMapping("/products/csv")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<String> exportProductsCsv() {
        String csvData = reportService.generateProductsCsv();
        
        String filename = "products_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", filename);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);
    }

    // Export low stock products to CSV
    @GetMapping("/low-stock/csv")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> exportLowStockCsv() {
        String csvData = reportService.generateLowStockCsv();
        
        String filename = "low_stock_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", filename);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);
    }
}