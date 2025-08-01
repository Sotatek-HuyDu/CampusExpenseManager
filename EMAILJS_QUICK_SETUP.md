# EmailJS Quick Setup Guide

## 🚀 Quick Setup (5 minutes)

### 1. Sign Up
- Go to [EmailJS.com](https://www.emailjs.com/)
- Sign up for free account (200 emails/month)

### 2. Create Email Service
- Dashboard → **Email Services** → **Add New Service**
- Choose **Gmail** (or your preferred provider)
- Connect your email account
- Copy **Service ID** (e.g., `service_abc123`)

### 3. Create Template
- Dashboard → **Email Templates** → **Create New Template**
- **Subject:** `Password Reset Code - {{app_name}}`
- **HTML Content:** Use the template from `EMAIL_SETUP_GUIDE.md`
- Save and copy **Template ID** (e.g., `template_xyz789`)

### 4. Get API Key
- Dashboard → **Account** → **API Keys**
- Copy **Public Key** (e.g., `user_def456`)

### 5. Update Code
Replace in `EmailService.java`:
```java
private static final String EMAILJS_SERVICE_ID = "service_abc123";
private static final String EMAILJS_TEMPLATE_ID = "template_xyz789";
private static final String EMAILJS_PUBLIC_KEY = "user_def456";
```

## ✅ Done!
Your email system is now ready to send password reset codes!

## 🔧 Template Variables
The app sends these variables to your template:
- `{{to_email}}` - User's email address
- `{{reset_code}}` - 4-digit reset code
- `{{app_name}}` - "Campus Expense Manager"
- `{{expiry_time}}` - "10 minutes"

## 🧪 Testing
1. Add your email in the app
2. Click "Reset Password"
3. Check your email for the reset code
4. If email fails, code shows in toast (fallback)

## 📞 Support
- EmailJS Dashboard: [dashboard.emailjs.com](https://dashboard.emailjs.com/)
- EmailJS Docs: [emailjs.com/docs](https://www.emailjs.com/docs/) 