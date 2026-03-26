package com.inventory.inventory_system.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "notification_preferences")
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    private boolean lowStockAlerts = true;
    private boolean welcomeEmails = true;
    private boolean stockRestoredAlerts = false;

    public NotificationPreference() {}

    public NotificationPreference(User user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isLowStockAlerts() {
        return lowStockAlerts;
    }

    public void setLowStockAlerts(boolean lowStockAlerts) {
        this.lowStockAlerts = lowStockAlerts;
    }

    public boolean isWelcomeEmails() {
        return welcomeEmails;
    }

    public void setWelcomeEmails(boolean welcomeEmails) {
        this.welcomeEmails = welcomeEmails;
    }

    public boolean isStockRestoredAlerts() {
        return stockRestoredAlerts;
    }

    public void setStockRestoredAlerts(boolean stockRestoredAlerts) {
        this.stockRestoredAlerts = stockRestoredAlerts;
    }
}