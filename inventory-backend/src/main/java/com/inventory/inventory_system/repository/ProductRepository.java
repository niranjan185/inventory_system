package com.inventory.inventory_system.repository;

import com.inventory.inventory_system.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategoryContainingIgnoreCase(String category);
    
    @Query("SELECT p FROM Product p WHERE p.reorderLevel IS NOT NULL AND p.quantity <= p.reorderLevel")
    List<Product> findLowStockProducts();
}