# Inventory Management System API Documentation

## Authentication Endpoints

### POST /api/auth/register

Register a new user

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

Update product (ADMIN only)

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

## Admin User Management (ADMIN only)

### GET /api/admin/users

Get all users

### GET /api/admin/users/{id}

Get user by ID

### PUT /api/admin/users/{id}/role?role=ADMIN

Update user role

### DELETE /api/admin/users/{id}

Delete user

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

- **USER**: Can view products and reports
- **ADMIN**: Full access to all endpoints
