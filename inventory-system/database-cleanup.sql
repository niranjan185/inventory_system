-- Database Cleanup Script for Inventory System
-- Run this script to completely clean the database and remove corrupted data

-- Disable foreign key checks temporarily
SET FOREIGN_KEY_CHECKS = 0;

-- Clean up all tables
DELETE FROM inventory_movements;
DELETE FROM audit_logs;
DELETE FROM alerts;
DELETE FROM low_stock_logs;
DELETE FROM notification_preferences;
DELETE FROM password_reset_tokens;
DELETE FROM products;
DELETE FROM users;

-- Reset auto-increment counters
ALTER TABLE inventory_movements AUTO_INCREMENT = 1;
ALTER TABLE audit_logs AUTO_INCREMENT = 1;
ALTER TABLE alerts AUTO_INCREMENT = 1;
ALTER TABLE low_stock_logs AUTO_INCREMENT = 1;
ALTER TABLE notification_preferences AUTO_INCREMENT = 1;
ALTER TABLE password_reset_tokens AUTO_INCREMENT = 1;
ALTER TABLE products AUTO_INCREMENT = 1;
ALTER TABLE users AUTO_INCREMENT = 1;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Verify cleanup
SELECT 'Products' as table_name, COUNT(*) as count FROM products
UNION ALL
SELECT 'Users' as table_name, COUNT(*) as count FROM users
UNION ALL
SELECT 'Alerts' as table_name, COUNT(*) as count FROM alerts
UNION ALL
SELECT 'Audit Logs' as table_name, COUNT(*) as count FROM audit_logs
UNION ALL
SELECT 'Inventory Movements' as table_name, COUNT(*) as count FROM inventory_movements
UNION ALL
SELECT 'Low Stock Logs' as table_name, COUNT(*) as count FROM low_stock_logs
UNION ALL
SELECT 'Notification Preferences' as table_name, COUNT(*) as count FROM notification_preferences;

-- All counts should be 0 after cleanup