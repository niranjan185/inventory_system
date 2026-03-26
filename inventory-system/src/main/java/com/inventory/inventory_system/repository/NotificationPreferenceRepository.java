package com.inventory.inventory_system.repository;

import com.inventory.inventory_system.entity.NotificationPreference;
import com.inventory.inventory_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {
    
    Optional<NotificationPreference> findByUser(User user);
    
    Optional<NotificationPreference> findByUserId(Long userId);
}