# Email Setup Guide for OTP Password Reset

## Current Status

The OTP password reset system is fully implemented but emails are currently disabled for development. The OTP codes are displayed in the console logs when you request a password reset.

## Quick Test (Development Mode)

1. Start your Spring Boot application
2. Go to the forgot password page in your frontend
3. Enter a registered email address
4. Click "Send OTP"
5. Check the console logs - you'll see the OTP printed like this:

```
================================================================================
EMAIL DISABLED - OTP FOR DEVELOPMENT:
User: John Doe (john@example.com)
OTP Code: 123456
Valid for: 10 minutes
================================================================================
```

6. Use this OTP code in the frontend to complete the password reset

## Enable Real Email Sending

### Step 1: Get Gmail App Password

1. Go to your Gmail account settings
2. Enable 2-Factor Authentication if not already enabled
3. Go to "App passwords" section
4. Generate a new app password for "Mail"
5. Copy the 16-character app password

### Step 2: Update Configuration

Edit `inventory-system/src/main/resources/application.properties`:

```properties
# Enable email sending
spring.mail.enabled=true

# Your Gmail credentials
spring.mail.username=your-email@gmail.com
spring.mail.password=your-16-char-app-password
```

### Step 3: Test Email Functionality

1. Restart your Spring Boot application
2. Try the forgot password flow
3. Check your email inbox for the OTP

## Troubleshooting

### OTP Not Received in Email

1. Check spam/junk folder
2. Verify Gmail app password is correct
3. Check console logs for email sending errors
4. Ensure Gmail account has 2FA enabled

### Console Shows OTP but Email Disabled

- Set `spring.mail.enabled=true` in application.properties
- Restart the application

### Email Authentication Errors

- Verify the Gmail app password (not your regular password)
- Check if 2-Factor Authentication is enabled on Gmail
- Try generating a new app password

## Security Notes

- Never commit real email credentials to version control
- Use environment variables for production deployment
- The OTP expires after 10 minutes for security
- Each user can request maximum 5 OTPs per hour
