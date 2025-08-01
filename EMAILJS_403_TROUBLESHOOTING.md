# EmailJS 403 Error Troubleshooting Guide

## 🔍 **403 Forbidden Error - Common Causes & Solutions**

### **1. Check Your EmailJS Dashboard**

**Go to:** [dashboard.emailjs.com](https://dashboard.emailjs.com/)

**Verify these items:**
- ✅ **Email Service** is connected and active
- ✅ **Email Template** exists and is published
- ✅ **API Keys** are correct

### **2. Verify Your Configuration**

**Current Settings:**
```java
EMAILJS_SERVICE_ID = "service_ze5atc1"
EMAILJS_TEMPLATE_ID = "template_ga2yjap" 
EMAILJS_PUBLIC_KEY = "lNdDTgzrCE70Ze8dl"
```

**Check in EmailJS Dashboard:**
1. **Email Services** → Copy the exact Service ID
2. **Email Templates** → Copy the exact Template ID  
3. **Account → API Keys** → Copy the exact Public Key

### **3. Common Issues & Fixes**

#### **Issue A: Template Not Published**
- **Problem:** Template exists but not published
- **Solution:** Go to Email Templates → Click "Publish" on your template

#### **Issue B: Email Service Not Connected**
- **Problem:** Service ID exists but email provider not connected
- **Solution:** Go to Email Services → Reconnect your Gmail/email provider

#### **Issue C: Wrong Template Parameters**
- **Problem:** Template expects different variable names
- **Solution:** Check your template variables in EmailJS dashboard

#### **Issue D: Account Limits**
- **Problem:** Free tier limit reached (200 emails/month)
- **Solution:** Check usage in Account → Usage

### **4. Template Variable Check**

**Your template should use these variables:**
```html
{{to_email}} - Recipient email
{{to_name}} - Recipient name  
{{reset_code}} - 4-digit code
{{app_name}} - App name
{{expiry_time}} - Expiry time
```

**Make sure your EmailJS template has these exact variable names!**

### **5. Test Your Template**

**In EmailJS Dashboard:**
1. Go to **Email Templates**
2. Click **Test** on your template
3. Fill in test values and send
4. Check if test email is received

### **6. Check EmailJS Logs**

**In EmailJS Dashboard:**
1. Go to **Activity** or **Logs**
2. Look for failed requests
3. Check error messages

### **7. Alternative: Use EmailJS Test Mode**

**Temporarily switch to test mode:**
```java
// Add this line for testing
emailData.put("test_mode", true);
```

### **8. Quick Fix Checklist**

- [ ] Template is published
- [ ] Email service is connected
- [ ] API keys are correct
- [ ] Template variables match
- [ ] Account has available emails
- [ ] No typos in IDs

### **9. Debug Information**

**The app now logs:**
- Request payload (JSON data sent)
- Response body (error details)
- Specific error messages

**Check Android Studio Logcat for:**
```
EmailService: EmailJS API error: 403 - [error details]
EmailService: Request payload: [JSON data]
```

### **10. Still Having Issues?**

**Try these steps:**
1. **Create a new template** with simple content
2. **Use a different email service** (Outlook instead of Gmail)
3. **Check EmailJS status** at [status.emailjs.com](https://status.emailjs.com/)
4. **Contact EmailJS support** if needed

## 🚀 **Quick Test**

**Test with minimal template:**
```html
Subject: Test Email
Content: Hello {{to_name}}, your code is {{reset_code}}
```

This will help isolate if the issue is with the template or configuration. 