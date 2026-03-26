package com.inventory.inventory_system.dto;

import java.time.LocalDateTime;

public class LowStockLogDTO {
    
    private Long id;
    private Long productId;
    private String productName;
    private String category;
    private int currentQuantity;
    private int reorderLevel;
    private double price;
    private String status;
    private LocalDateTime detectedAt;
    private LocalDateTime resolvedAt;
    private String resolvedBy;
    private String notes;
    private boolean isResolved;
    private int daysUnresolved;
    private double totalValue;
    private String severity; // CRITICAL, HIGH, MEDIUM, LOW
    
    public LowStockLogDTO() {}
    
    public LowStockLogDTO(Long id, Long productId, String productName, String category,
                         int currentQuantity, int reorderLevel, double price, String status,
                         LocalDateTime detectedAt, LocalDateTime resolvedAt, String resolvedBy,
                         String notes, boolean isResolved) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.currentQuantity = currentQuantity;
        this.reorderLevel = reorderLevel;
        this.price = price;
        this.status = status;
        this.detectedAt = detectedAt;
        this.resolvedAt = resolvedAt;
        this.resolvedBy = resolvedBy;
        this.notes = notes;
        this.isResolved = isResolved;
        
        // Calculate derived fields
        this.totalValue = price * currentQuantity;
        this.daysUnresolved = calculateDaysUnresolved();
        this.severity = calculateSeverity();
    }
    
    private int calculateDaysUnresolved() {
        if (isResolved && resolvedAt != null) {
            return (int) java.time.Duration.between(detectedAt, resolvedAt).toDays();
        } else {
            return (int) java.time.Duration.between(detectedAt, LocalDateTime.now()).toDays();
        }
    }
    
    private String calculateSeverity() {
        if ("OUT_OF_STOCK".equals(status)) return "CRITICAL";
        if (currentQuantity == 0) return "CRITICAL";
        if (currentQuantity <= reorderLevel / 2) return "HIGH";
        if (currentQuantity <= reorderLevel) return "MEDIUM";
        return "LOW";
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public int getCurrentQuantity() { return currentQuantity; }
    public void setCurrentQuantity(int currentQuantity) { this.currentQuantity = currentQuantity; }
    
    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getDetectedAt() { return detectedAt; }
    public void setDetectedAt(LocalDateTime detectedAt) { this.detectedAt = detectedAt; }
    
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    
    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public boolean isResolved() { return isResolved; }
    public void setResolved(boolean resolved) { this.isResolved = resolved; }
    
    public int getDaysUnresolved() { return daysUnresolved; }
    public void setDaysUnresolved(int daysUnresolved) { this.daysUnresolved = daysUnresolved; }
    
    public double getTotalValue() { return totalValue; }
    public void setTotalValue(double totalValue) { this.totalValue = totalValue; }
    
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
}