# Enhanced Email Notification System

## Overview

The inventory management system now features a comprehensive dual-audience email notification system that sends targeted messages to both users and admins with different purposes and messaging.

## Notification Types

### 1. Low Stock Alerts

#### For Admins 🚨

- **Purpose**: Immediate action required for restocking
- **Subject**: "🚨 URGENT: Low Stock Alert - Restock Required"
- **Content**:
  - Critical stock information
  - Action items (restock, check suppliers, update reorder levels)
  - Professional, urgent tone
- **Recipient**: admin@inventory.com

#### For Users ⚡

- **Purpose**: Create urgency to encourage quick purchases
- **Subject**: "⚡ Limited Stock Alert - [Product Name]"
- **Content**:
  - "GRAB IT FAST" messaging
  - Excitement and urgency
  - Encourages immediate action
- **Recipients**: All users (based on notification preferences)

### 2. Stock Restored Notifications

#### For Admins ✅

- **Purpose**: Confirmation that stock levels are healthy
- **Subject**: "✅ Stock Restored - [Product Name]"
- **Content**: Professional confirmation of stock restoration

#### For Users ✅

- **Purpose**: Notify interested users that items are back
- **Subject**: "✅ Good News! [Product Name] is Back in Stock"
- **Content**: Encouraging "back in stock" message
- **Recipients**: Only users who opted in for stock restored alerts

### 3. Welcome Emails

- **Purpose**: Onboard new users
- **Subject**: "Welcome to Inventory Management System"
- **Content**: Account details, available features, getting started info
- **Recipients**: New users (if enabled in preferences)

## User Notification Preferences

Users can control which emails they receive through the `/api/notifications/preferences` endpoint:

```json
{
  "lowStockAlerts": true, // Default: enabled
  "welcomeEmails": true, // Default: enabled
  "stockRestoredAlerts": false // Default: disabled
}
```

## Technical Implementation

### Email Service Features

- **Dual messaging**: Different content for admins vs users
- **Preference checking**: Respects user notification settings
- **Error handling**: Graceful failure with logging
- **Bulk notifications**: Efficiently sends to multiple users

### Automatic Triggers

- **Product updates**: When stock goes below reorder level
- **Stock restoration**: When stock goes above reorder level after being low
- **User registration**: Welcome email for new users

### Manual Controls

- **Test emails**: Admins can send test notifications
- **Preference management**: Users can update their settings
- **Alert management**: Admins can create custom alerts

## Business Benefits

### For Users

- **Urgency creation**: "Grab it fast" messaging drives quick purchases
- **Personalized experience**: Users control what emails they receive
- **Timely notifications**: Know when items are low or back in stock

### For Admins

- **Immediate alerts**: Critical stock issues get urgent attention
- **Action-oriented**: Clear next steps for restocking
- **Confirmation**: Know when stock levels are restored
- **Testing capability**: Verify email functionality

### For Business

- **Increased sales**: Urgency messaging drives faster purchases
- **Better inventory management**: Admins get actionable alerts
- **Customer satisfaction**: Users stay informed about product availability
- **Reduced stockouts**: Proactive restocking based on alerts

## Configuration

### Email Settings (application.properties)

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### Admin Email

Currently hardcoded to `admin@inventory.com` - can be made configurable through application properties.

## API Endpoints

### Notification Management

- `GET /api/notifications/preferences` - Get user preferences
- `PUT /api/notifications/preferences` - Update user preferences
- `POST /api/notifications/test-email` - Send test email (Admin only)

### Automatic Triggers

- Product updates automatically trigger appropriate notifications
- User registration automatically sends welcome email (if enabled)

## Future Enhancements

1. **Email Templates**: HTML email templates for better formatting
2. **Configurable Admin Email**: Make admin email configurable
3. **Email Analytics**: Track open rates and engagement
4. **SMS Notifications**: Add SMS option for critical alerts
5. **Scheduled Digests**: Weekly/monthly inventory summaries
6. **Category-specific Preferences**: Allow users to choose categories they're interested in
