# Inventory Management System API Documentation

## Password Reset Endpoints

### POST /api/password-reset/request

Request password reset (sends email with reset link)

```json
{
  "email": "user@example.com"
}
```

### GET /api/password-reset/validate/{token}

Validate password reset token
Returns: `{"valid": true/false, "email": "user@example.com", "name": "User Name"}`

### POST /api/password-reset/confirm

Reset password with token

```json
{
  "token": "reset-token-here",
  "newPassword": "newPassword123",
  "confirmPassword": "newPassword123"
}
```

### POST /api/password-reset/revoke/{userEmail}

Revoke all password reset tokens for a user (ADMIN only)

## Authentication Endpoints

### POST /api/auth/register

Register a new user (sends welcome email if enabled)

```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123"
}
```

### POST /api/auth/login

Login user

```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

### POST /api/auth/admin-login

Admin login

```json
{
  "email": "admin@inventory.com",
  "password": "admin123"
}
```

## Product Endpoints (Requires Authentication)

### GET /api/products

Get all products (USER/ADMIN)

### GET /api/products/category/{category}

Get products by category (USER/ADMIN)

### GET /api/products/low-stock

Get low stock products (ADMIN only)

### POST /api/products

Add new product (ADMIN only)

```json
{
  "name": "Product Name",
  "category": "Electronics",
  "price": 99.99,
  "quantity": 50,
  "reorderLevel": 10
}
```

### PUT /api/products/{id}

Update product (ADMIN only) - triggers email notifications if stock goes low or is restored

### PUT /api/products/bulk-update

Bulk update multiple products (ADMIN only)

```json
[
  {
    "id": 1,
    "name": "Product 1",
    "category": "Electronics",
    "price": 99.99,
    "quantity": 100,
    "reorderLevel": 10
  }
]
```

### DELETE /api/products/{id}

Delete product (ADMIN only)

## Alert Endpoints (ADMIN only)

### GET /api/alerts

Get all alerts

### GET /api/alerts/product/{productName}

Get alerts by product name

### POST /api/alerts

Create manual alert

```json
{
  "productName": "Product Name",
  "message": "Custom alert message"
}
```

### DELETE /api/alerts/{id}

Delete specific alert

### DELETE /api/alerts/clear

Clear all alerts

## Report Endpoints

### GET /api/reports/inventory

Get inventory summary report (USER/ADMIN)

### GET /api/reports/products/csv

Export all products to CSV (ADMIN only)

### GET /api/reports/low-stock/csv

Export low stock products to CSV (ADMIN only)

## Notification Endpoints

### GET /api/notifications/preferences

Get user's notification preferences (USER/ADMIN)

### PUT /api/notifications/preferences

Update user's notification preferences (USER/ADMIN)

```json
{
  "lowStockAlerts": true,
  "welcomeEmails": true,
  "stockRestoredAlerts": false
}
```

### POST /api/notifications/test-email?email=test@example.com&type=lowstock

Send test email (ADMIN only)
Types: welcome, lowstock, restored

## Admin User Management (ADMIN only)

### GET /api/admin/users

Get all users

### GET /api/admin/users/{id}

Get user by ID

### PUT /api/admin/users/{id}/role?role=ADMIN

Update user role

### DELETE /api/admin/users/{id}

Delete user

## Email Notification System

### Low Stock Alerts

**Admin Email**: Urgent notification with action items for restocking
**User Email**: Exciting "grab it fast" message to encourage quick purchases

### Stock Restored Alerts

**Admin Email**: Confirmation that stock levels are healthy
**User Email**: "Back in stock" notification (only if user opted in)

### Welcome Emails

Sent to new users upon registration (if enabled in preferences)

### Notification Preferences

Users can control which emails they receive:

- Low stock alerts (default: enabled)
- Welcome emails (default: enabled)
- Stock restored alerts (default: disabled)

## Authentication Headers

All protected endpoints require:

```
Authorization: Bearer <JWT_TOKEN>
```

## Response Format

Success responses return data directly or with success message.
Error responses return:

```json
{
  "error": "Error message"
}
```

## User Roles

- **USER**: Can view products, reports, and manage notification preferences
- **ADMIN**: Full access to all endpoints including user management and bulk operations

## Email Configuration

Configure SMTP settings in `application.properties`:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password
```
