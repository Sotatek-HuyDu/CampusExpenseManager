# Email Setup Guide for Password Reset

## Option 1: EmailJS (Recommended - Easiest Setup)

### Step 1: Create EmailJS Account
1. Go to [EmailJS.com](https://www.emailjs.com/)
2. Sign up for a free account (200 emails/month free)
3. Verify your email address

### Step 2: Create Email Service
1. In EmailJS dashboard, go to **Email Services**
2. Click **Add New Service**
3. Choose your email provider (Gmail, Outlook, etc.)
4. Connect your email account
5. Copy the **Service ID** (e.g., `service_abc123`)

### Step 3: Create Email Template
1. Go to **Email Templates**
2. Click **Create New Template**
3. Use this template content:

**Subject:** Password Reset Code - {{app_name}}

**HTML Content:**
```html
<!DOCTYPE html>
<html>
<head>
    <meta charset='utf-8'>
    <title>Password Reset</title>
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background-color: #2C3E50; color: white; padding: 20px; text-align: center; }
        .content { padding: 20px; background-color: #f9f9f9; }
        .code { background-color: #3498DB; color: white; padding: 15px; text-align: center; font-size: 24px; font-weight: bold; margin: 20px 0; border-radius: 5px; }
        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
    </style>
</head>
<body>
    <div class='container'>
        <div class='header'>
            <h1>{{app_name}}</h1>
        </div>
        <div class='content'>
            <h2>Password Reset Request</h2>
            <p>You have requested to reset your password. Use the following code to complete the process:</p>
            <div class='code'>{{reset_code}}</div>
            <p><strong>Important:</strong></p>
            <ul>
                <li>This code will expire in {{expiry_time}}</li>
                <li>If you didn't request this reset, please ignore this email</li>
                <li>Never share this code with anyone</li>
            </ul>
            <p>If you have any questions, please contact our support team.</p>
        </div>
        <div class='footer'>
            <p>This is an automated message. Please do not reply to this email.</p>
        </div>
    </div>
</body>
</html>
```

4. Save the template and copy the **Template ID** (e.g., `template_xyz789`)

### Step 4: Get Public Key
1. Go to **Account > API Keys**
2. Copy your **Public Key** (e.g., `user_def456`)

### Step 5: Update EmailService.java
Replace the placeholder values in `EmailService.java`:

```java
private static final String EMAILJS_SERVICE_ID = "YOUR_SERVICE_ID"; // e.g., "service_abc123"
private static final String EMAILJS_TEMPLATE_ID = "YOUR_TEMPLATE_ID"; // e.g., "template_xyz789"
private static final String EMAILJS_PUBLIC_KEY = "YOUR_PUBLIC_KEY"; // e.g., "user_def456"
```

## Option 2: Gmail SMTP (Alternative)

If you prefer to use Gmail directly, you can modify the EmailService to use SMTP:

### Step 1: Enable 2-Factor Authentication on Gmail
1. Go to your Google Account settings
2. Enable 2-Factor Authentication

### Step 2: Generate App Password
1. Go to **Security > 2-Step Verification > App passwords**
2. Generate a new app password for "Mail"
3. Copy the 16-character password

### Step 3: Update EmailService
Replace the EmailJS implementation with Gmail SMTP:

```java
// Add these dependencies to build.gradle.kts:
implementation("com.sun.mail:android-mail:1.6.7")
implementation("com.sun.mail:android-activation:1.6.7")
```

## Option 3: Firebase Functions (Most Professional)

### Step 1: Install Firebase CLI
```bash
npm install -g firebase-tools
```

### Step 2: Initialize Firebase Functions
```bash
firebase login
firebase init functions
```

### Step 3: Create Email Function
Create `functions/index.js`:

```javascript
const functions = require('firebase-functions');
const nodemailer = require('nodemailer');

exports.sendPasswordResetEmail = functions.https.onCall(async (data, context) => {
    const { email, resetCode } = data;
    
    const transporter = nodemailer.createTransporter({
        service: 'gmail',
        auth: {
            user: 'your-email@gmail.com',
            pass: 'your-app-password'
        }
    });
    
    const mailOptions = {
        from: 'your-email@gmail.com',
        to: email,
        subject: 'Password Reset Code',
        html: `<h1>Your reset code is: ${resetCode}</h1>`
    };
    
    await transporter.sendMail(mailOptions);
    return { success: true };
});
```

### Step 4: Deploy Function
```bash
firebase deploy --only functions
```

## Testing

### Current Implementation
- The app will try to send email via EmailJS
- If it fails, it will show the code in a toast as fallback
- This allows you to test the flow while setting up email

### Testing Steps
1. Add your email address in the app
2. Click "Reset Password"
3. Check if email is received
4. If not, the code will be shown in toast

## Security Notes

1. **Never commit API keys to version control**
2. **Use environment variables for production**
3. **Consider rate limiting for email sending**
4. **Add email validation before sending**

## Troubleshooting

### EmailJS Issues
- Check if Service ID, Template ID, and Public Key are correct
- Verify email service is connected properly
- Check EmailJS dashboard for errors

### Gmail Issues
- Ensure 2FA is enabled
- Use App Password, not regular password
- Check Gmail security settings

### Network Issues
- Add internet permission to AndroidManifest.xml
- Check device internet connection
- Verify firewall settings

## Production Considerations

1. **Email Templates**: Create professional HTML templates
2. **Rate Limiting**: Limit email sending frequency
3. **Error Handling**: Implement proper error recovery
4. **Logging**: Add email sending logs for debugging
5. **Monitoring**: Set up email delivery monitoring 