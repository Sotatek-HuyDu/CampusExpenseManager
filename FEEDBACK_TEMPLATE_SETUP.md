# Feedback Email Template Setup

## 📧 **EmailJS Template Configuration**

### **Step 1: Create New Template**
1. Go to [EmailJS Dashboard](https://dashboard.emailjs.com/)
2. Navigate to **Templates** section
3. Click **"Create New Template"**

### **Step 2: Template Configuration**
- **Template Name**: `Feedback Template`
- **Subject**: `New Feedback from Campus Expense Manager`
- **Service**: Use your existing service (`service_ze5atc1`)

### **Step 3: Template Content**
Use this HTML template:

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Feedback from Campus Expense Manager</title>
</head>
<body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
        <h2 style="color: #2196F3; border-bottom: 2px solid #2196F3; padding-bottom: 10px;">
            📱 New Feedback Received
        </h2>
        
        <div style="background: #f5f5f5; padding: 20px; border-radius: 8px; margin: 20px 0;">
            <h3 style="margin-top: 0; color: #333;">User Information</h3>
            
            <div style="margin: 15px 0;">
                <strong>Name:</strong> {{from_name}}
            </div>
            
            <div style="margin: 15px 0;">
                <strong>Email:</strong> {{from_email}}
            </div>
            
            <div style="margin: 15px 0;">
                <strong>App:</strong> {{app_name}}
            </div>
        </div>
        
        <div style="background: #f5f5f5; padding: 20px; border-radius: 8px; margin: 20px 0;">
            <h3 style="margin-top: 0; color: #333;">Detailed Ratings</h3>
            
            <div style="margin: 15px 0;">
                <strong>1. Overall Satisfaction:</strong> 
                <span style="color: #FF9800; font-size: 16px;">
                    {{rating_overall}}/5 ⭐
                </span>
            </div>
            
            <div style="margin: 15px 0;">
                <strong>2. Design & Layout:</strong> 
                <span style="color: #FF9800; font-size: 16px;">
                    {{rating_design}}/5 ⭐
                </span>
            </div>
            
            <div style="margin: 15px 0;">
                <strong>3. Navigation Ease:</strong> 
                <span style="color: #FF9800; font-size: 16px;">
                    {{rating_navigation}}/5 ⭐
                </span>
            </div>
            
            <div style="margin: 15px 0;">
                <strong>4. Functionality:</strong> 
                <span style="color: #FF9800; font-size: 16px;">
                    {{rating_functionality}}/5 ⭐
                </span>
            </div>
            
            <div style="margin: 15px 0;">
                <strong>5. Recommendation Likelihood:</strong> 
                <span style="color: #FF9800; font-size: 16px;">
                    {{rating_recommendation}}/5 ⭐
                </span>
            </div>
        </div>
        
        <div style="background: #f5f5f5; padding: 20px; border-radius: 8px; margin: 20px 0;">
            <h3 style="margin-top: 0; color: #333;">Additional Recommendations</h3>
            
            <div style="background: white; padding: 15px; border-radius: 5px; margin-top: 10px; border-left: 4px solid #2196F3;">
                {{feedback}}
            </div>
        </div>
        
        <div style="text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; color: #666; font-size: 12px;">
            This feedback was sent from the Campus Expense Manager mobile app.
        </div>
    </div>
</body>
</html>
```

### **Step 4: Template Variables**
Make sure your template includes these variables:
- `{{from_name}}` - User's name
- `{{from_email}}` - User's email address
- `{{rating_overall}}` - Overall satisfaction rating (1-5)
- `{{rating_design}}` - Design & layout rating (1-5)
- `{{rating_navigation}}` - Navigation ease rating (1-5)
- `{{rating_functionality}}` - Functionality rating (1-5)
- `{{rating_recommendation}}` - Recommendation likelihood (1-5)
- `{{feedback}}` - Additional recommendations text
- `{{app_name}}` - App name ("Campus Expense Manager")

### **Step 5: Update App Configuration**
After creating the template, update the `FEEDBACK_TEMPLATE_ID` in `EmailService.java`:

```java
private static final String FEEDBACK_TEMPLATE_ID = "template_YOUR_NEW_TEMPLATE_ID";
```

### **Step 6: Test the Template**
1. Save and publish the template
2. Test the feedback feature in your app
3. Check if you receive the email at `duquanghuy@gmail.com`

## ✅ **Current Configuration**
- **Service ID**: `service_ze5atc1`
- **Public Key**: `lNdDTgzrCE70Ze8dl`
- **Password Reset Template**: `template_ga2yjap`
- **Feedback Template**: `template_feedback` (replace with your new template ID)

## 🔧 **Troubleshooting**
- Make sure the template is **published**
- Verify all template variables are correctly named
- Check that the service is connected to your email provider
- Test with the EmailJS dashboard first before using in the app 