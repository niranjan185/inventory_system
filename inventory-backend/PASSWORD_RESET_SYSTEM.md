# Password Reset System Documentation

## 🔐 Overview

Comprehensive password reset functionality with enterprise-grade security features including rate limiting, token expiration, audit logging, and email notifications.

## 🏗️ Architecture Components

### 1. **PasswordResetToken Entity**

- Secure token storage with expiration (1 hour)
- User association and usage tracking
- IP address logging for security
- Automatic cleanup of expired tokens

### 2. **Security Features**

- **Rate Limiting**: Max 3 reset attempts per hour per user
- **Secure Token Generation**: 32-byte cryptographically secure random tokens
- **Token Expiration**: 1-hour validity window
- **Single Use**: Tokens are marked as used after password reset
- **IP Tracking**: All requests logged with IP addresses

### 3. **Email Integration**

- **Reset Request Email**: Secure link with token
- **Confirmation Email**: Success notification
- **Security Warnings**: Instructions for unauthorized requests

### 4. **Audit Logging**

- All password reset activities logged
- IP address tracking
- Admin token revocation logging
- Failed attempt monitoring

## 🔄 Password Reset Flow

### Step 1: Request Reset

```
POST /api/password-reset/request
{
  "email": "user@example.com"
}
```

**Process:**

1. Validate email format
2. Check rate limiting (3 attempts/hour)
3. Generate secure token
4. Invalidate existing tokens
5. Send reset email
6. Log audit trail

**Security Notes:**

- Returns success even for non-existent emails (prevents email enumeration)
- Rate limiting prevents brute force attacks
- All existing tokens invalidated for security

### Step 2: Validate Token

```
GET /api/password-reset/validate/{token}
```

**Returns:**

```json
{
  "valid": true,
  "email": "user@example.com",
  "name": "User Name"
}
```

### Step 3: Reset Password

```
POST /api/password-reset/confirm
{
  "token": "secure-token-here",
  "newPassword": "newPassword123",
  "confirmPassword": "newPassword123"
}
```

**Process:**

1. Validate token (not expired, not used)
2. Verify password match
3. Hash new password with BCrypt
4. Update user password
5. Mark token as used
6. Send confirmation email
7. Log audit trail

## 🛡️ Security Features

### Rate Limiting

- **Limit**: 3 password reset requests per hour per user
- **Purpose**: Prevent abuse and brute force attacks
- **Implementation**: Database-based tracking with time windows

### Token Security

- **Generation**: Cryptographically secure random 32-byte tokens
- **Encoding**: Base64 URL-safe encoding
- **Expiration**: 1 hour from generation
- **Single Use**: Tokens invalidated after successful reset

### Audit Trail

- **Request Logging**: All reset requests logged with IP
- **Success/Failure**: All outcomes tracked
- **Admin Actions**: Token revocation by admins logged
- **Security Events**: Failed attempts and suspicious activity

### Email Security

- **No Sensitive Data**: Emails don't contain passwords
- **Clear Instructions**: Users warned about unauthorized requests
- **Secure Links**: Tokens only in URL parameters
- **Confirmation**: Success notifications sent

## 🔧 Administrative Features

### Token Management

```
POST /api/password-reset/revoke/{userEmail}
```

- Admins can revoke all tokens for a user
- Useful for security incidents
- Fully audited with admin identification

### Automatic Cleanup

- **Scheduled Task**: Runs every hour
- **Purpose**: Remove expired tokens from database
- **Performance**: Keeps token table clean

## 🌐 Frontend Integration

### HTML Interface

- **Location**: `/reset-password.html`
- **Features**:
  - Request reset form
  - Token validation
  - Password reset form
  - Real-time feedback
  - Responsive design

### API Integration

```javascript
// Request password reset
const response = await fetch("/api/password-reset/request", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({ email: "user@example.com" }),
});

// Validate token
const validation = await fetch(`/api/password-reset/validate/${token}`);

// Reset password
const reset = await fetch("/api/password-reset/confirm", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({
    token: token,
    newPassword: "newPassword123",
    confirmPassword: "newPassword123",
  }),
});
```

## 📧 Email Templates

### Reset Request Email

- **Subject**: "🔐 Password Reset Request"
- **Content**: Secure reset link with 1-hour expiration
- **Security**: Clear warnings about unauthorized requests

### Confirmation Email

- **Subject**: "✅ Password Reset Successful"
- **Content**: Success confirmation with security tips
- **Action**: Advises immediate contact if unauthorized

## 🔍 Monitoring & Logging

### Key Metrics to Monitor

- Password reset request frequency
- Failed token validation attempts
- Rate limiting triggers
- Token expiration rates

### Log Entries

- `PASSWORD_RESET_REQUESTED`: User requested reset
- `PASSWORD_RESET_COMPLETED`: Password successfully changed
- `PASSWORD_RESET_TOKENS_REVOKED`: Admin revoked tokens

### Security Alerts

- Multiple failed attempts from same IP
- High volume of requests for single user
- Token validation failures

## 🚀 Production Considerations

### Environment Configuration

```properties
# Custom expiration time (in hours)
app.password-reset.expiration-hours=1

# Rate limiting
app.password-reset.max-attempts-per-hour=3

# Frontend URL for email links
app.frontend.url=https://yourdomain.com
```

### Database Indexes

```sql
-- Recommended indexes for performance
CREATE INDEX idx_password_reset_token ON password_reset_tokens(token);
CREATE INDEX idx_password_reset_user_created ON password_reset_tokens(user_id, created_at);
CREATE INDEX idx_password_reset_expiry ON password_reset_tokens(expiry_date);
```

### Monitoring Queries

```sql
-- Active tokens
SELECT COUNT(*) FROM password_reset_tokens WHERE used = false AND expiry_date > NOW();

-- Reset requests in last 24 hours
SELECT COUNT(*) FROM password_reset_tokens WHERE created_at > NOW() - INTERVAL 24 HOUR;
```

## ✅ Security Compliance

### OWASP Guidelines

- ✅ Secure token generation
- ✅ Rate limiting implementation
- ✅ Token expiration
- ✅ Audit logging
- ✅ No sensitive data in emails

### Enterprise Security

- ✅ IP address tracking
- ✅ Admin override capabilities
- ✅ Comprehensive audit trails
- ✅ Automatic cleanup processes
- ✅ Secure password hashing

The password reset system is now production-ready with enterprise-grade security features!
