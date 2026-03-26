package com.inventory.inventory_system.dto;

import jakarta.validation.constraints.*;

public class ProductDTO {
    
    private Long id;
    
    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    private String name;
    
    @NotBlank(message = "Category is required")
    @Size(min = 2, max = 50, message = "Category must be between 2 and 50 characters")
    private String category;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Price cannot exceed 999,999.99")
    private Double price;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    @Max(value = 999999, message = "Quantity cannot exceed 999,999")
    private Integer quantity;
    
    @NotNull(message = "Reorder level is required")
    @Min(value = 0, message = "Reorder level cannot be negative")
    @Max(value = 9999, message = "Reorder level cannot exceed 9,999")
    private Integer reorderLevel;
    
    private String stockStatus; // LOW_STOCK, IN_STOCK, OUT_OF_STOCK
    private Double totalValue; // price * quantity
    
    public ProductDTO() {}
    
    public ProductDTO(Long id, String name, String category, Double price, 
                     Integer quantity, Integer reorderLevel) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
        this.reorderLevel = reorderLevel;
        this.totalValue = price * quantity;
        this.stockStatus = determineStockStatus();
    }
    
    private String determineStockStatus() {
        if (quantity == null || quantity == 0) return "OUT_OF_STOCK";
        if (reorderLevel != null && quantity <= reorderLevel) return "LOW_STOCK";
        return "IN_STOCK";
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public Double getPrice() { return price; }
    public void setPrice(Double price) { 
        this.price = price;
        if (quantity != null && price != null) this.totalValue = price * quantity;
    }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { 
        this.quantity = quantity;
        if (price != null && quantity != null) this.totalValue = price * quantity;
        this.stockStatus = determineStockStatus();
    }
    
    public Integer getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(Integer reorderLevel) { 
        this.reorderLevel = reorderLevel;
        this.stockStatus = determineStockStatus();
    }
    
    public String getStockStatus() { return stockStatus; }
    public void setStockStatus(String stockStatus) { this.stockStatus = stockStatus; }
    
    public Double getTotalValue() { return totalValue; }
    public void setTotalValue(Double totalValue) { this.totalValue = totalValue; }
}