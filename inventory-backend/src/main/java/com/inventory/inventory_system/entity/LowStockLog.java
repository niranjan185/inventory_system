package com.inventory.inventory_system.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "low_stock_logs")
public class LowStockLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private int currentQuantity;

    @Column(nullable = false)
    private int reorderLevel;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private String status; // LOW_STOCK, OUT_OF_STOCK, CRITICAL

    @Column(nullable = false)
    private LocalDateTime detectedAt;

    @Column
    private LocalDateTime resolvedAt;

    @Column
    private String resolvedBy;

    @Column
    private String notes;

    @Column(nullable = false)
    private boolean isResolved = false;

    public LowStockLog() {
        this.detectedAt = LocalDateTime.now();
    }

    public LowStockLog(Long productId, String productName, String category, 
                      int currentQuantity, int reorderLevel, double price, String status) {
        this();
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.currentQuantity = currentQuantity;
        this.reorderLevel = reorderLevel;
        this.price = price;
        this.status = status;
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
    public void setResolved(boolean resolved) { 
        this.isResolved = resolved;
        if (resolved && this.resolvedAt == null) {
            this.resolvedAt = LocalDateTime.now();
        }
    }
}