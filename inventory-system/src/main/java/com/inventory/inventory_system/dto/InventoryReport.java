package com.inventory.inventory_system.dto;

import java.time.LocalDateTime;
import java.util.List;

public class InventoryReport {
    
    private LocalDateTime generatedAt;
    private int totalProducts;
    private int lowStockProducts;
    private double totalInventoryValue;
    private List<CategorySummary> categorySummaries;
    
    public InventoryReport() {
        this.generatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }
    
    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
    
    public int getTotalProducts() {
        return totalProducts;
    }
    
    public void setTotalProducts(int totalProducts) {
        this.totalProducts = totalProducts;
    }
    
    public int getLowStockProducts() {
        return lowStockProducts;
    }
    
    public void setLowStockProducts(int lowStockProducts) {
        this.lowStockProducts = lowStockProducts;
    }
    
    public double getTotalInventoryValue() {
        return totalInventoryValue;
    }
    
    public void setTotalInventoryValue(double totalInventoryValue) {
        this.totalInventoryValue = totalInventoryValue;
    }
    
    public List<CategorySummary> getCategorySummaries() {
        return categorySummaries;
    }
    
    public void setCategorySummaries(List<CategorySummary> categorySummaries) {
        this.categorySummaries = categorySummaries;
    }
    
    public static class CategorySummary {
        private String category;
        private int productCount;
        private double totalValue;
        
        public CategorySummary() {}
        
        public CategorySummary(String category, int productCount, double totalValue) {
            this.category = category;
            this.productCount = productCount;
            this.totalValue = totalValue;
        }
        
        public String getCategory() {
            return category;
        }
        
        public void setCategory(String category) {
            this.category = category;
        }
        
        public int getProductCount() {
            return productCount;
        }
        
        public void setProductCount(int productCount) {
            this.productCount = productCount;
        }
        
        public double getTotalValue() {
            return totalValue;
        }
        
        public void setTotalValue(double totalValue) {
            this.totalValue = totalValue;
        }
    }
}