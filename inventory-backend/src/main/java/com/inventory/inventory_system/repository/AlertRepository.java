package com.inventory.inventory_system.repository;

import com.inventory.inventory_system.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findAllByOrderByCreatedAtDesc();
    
    List<Alert> findByProductNameContainingIgnoreCaseOrderByCreatedAtDesc(String productName);
}