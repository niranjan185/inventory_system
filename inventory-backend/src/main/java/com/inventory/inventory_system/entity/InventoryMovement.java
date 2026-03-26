package com.inventory.inventory_system.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_movements")
public class InventoryMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String movementType; // IN, OUT, ADJUSTMENT

    @Column(nullable = false)
    private int quantityBefore;

    @Column(nullable = false)
    private int quantityChanged;

    @Column(nullable = false)
    private int quantityAfter;

    private String reason; // PURCHASE, SALE, DAMAGE, ADJUSTMENT, etc.

    private String userEmail;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private String notes;

    public InventoryMovement() {
        this.timestamp = LocalDateTime.now();
    }

    public InventoryMovement(Product product, String movementType, int quantityBefore, 
                           int quantityChanged, int quantityAfter, String reason, 
                           String userEmail, String notes) {
        this();
        this.product = product;
        this.movementType = movementType;
        this.quantityBefore = quantityBefore;
        this.quantityChanged = quantityChanged;
        this.quantityAfter = quantityAfter;
        this.reason = reason;
        this.userEmail = userEmail;
        this.notes = notes;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public String getMovementType() { return movementType; }
    public void setMovementType(String movementType) { this.movementType = movementType; }

    public int getQuantityBefore() { return quantityBefore; }
    public void setQuantityBefore(int quantityBefore) { this.quantityBefore = quantityBefore; }

    public int getQuantityChanged() { return quantityChanged; }
    public void setQuantityChanged(int quantityChanged) { this.quantityChanged = quantityChanged; }

    public int getQuantityAfter() { return quantityAfter; }
    public void setQuantityAfter(int quantityAfter) { this.quantityAfter = quantityAfter; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}