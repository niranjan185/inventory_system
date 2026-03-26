package com.inventory.inventory_system.controller;

import com.inventory.inventory_system.entity.InventoryMovement;
import com.inventory.inventory_system.service.InventoryMovementService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/inventory-movements")
@CrossOrigin(origins = "*")
public class InventoryMovementController {

    @Autowired
    private InventoryMovementService inventoryMovementService;

    // Get all movements (last 50) - Admin only
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<InventoryMovement> getAllMovements() {
        return inventoryMovementService.getAllMovements();
    }

    // Get movements by product - User/Admin
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<InventoryMovement> getMovementsByProduct(@PathVariable Long productId) {
        return inventoryMovementService.getMovementsByProduct(productId);
    }

    // Get movements by type - Admin only
    @GetMapping("/type/{movementType}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<InventoryMovement> getMovementsByType(@PathVariable String movementType) {
        return inventoryMovementService.getMovementsByType(movementType);
    }

    // Get movements by user - Admin only
    @GetMapping("/user/{userEmail}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<InventoryMovement> getMovementsByUser(@PathVariable String userEmail) {
        return inventoryMovementService.getMovementsByUser(userEmail);
    }

    // Get movements by date range - Admin only
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InventoryMovement>> getMovementsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().build();
        }
        
        List<InventoryMovement> movements = inventoryMovementService.getMovementsByDateRange(startDate, endDate);
        return ResponseEntity.ok(movements);
    }

    // Get movements by product and date range - User/Admin
    @GetMapping("/product/{productId}/date-range")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<InventoryMovement>> getMovementsByProductAndDateRange(
            @PathVariable Long productId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().build();
        }
        
        List<InventoryMovement> movements = inventoryMovementService.getMovementsByProductAndDateRange(
                productId, startDate, endDate);
        return ResponseEntity.ok(movements);
    }
}