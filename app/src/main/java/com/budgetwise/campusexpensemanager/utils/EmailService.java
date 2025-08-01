package com.budgetwise.campusexpensemanager.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.webkit.JavascriptInterface;
import android.os.Handler;
import android.os.Looper;

public class EmailService {
    private static final String TAG = "EmailService";
    // EmailJS Configuration
    private static final String EMAILJS_SERVICE_ID = "service_ze5atc1";
    private static final String EMAILJS_PUBLIC_KEY = "lNdDTgzrCE70Ze8dl";
    
    // Template IDs
    private static final String PASSWORD_RESET_TEMPLATE_ID = "template_ga2yjap";
    private static final String FEEDBACK_TEMPLATE_ID = "template_y27v1iv"; // Replace with your new template ID
    
    public interface EmailCallback {
        void onSuccess();
        void onFailure(String error);
    }
    
    public static void sendPasswordResetEmail(Context context, String toEmail, String resetCode, EmailCallback callback) {
        new SendEmailTask(context, callback).execute(toEmail, resetCode);
    }
    
    public static void sendFeedbackEmail(Context context, String userEmail, String userName, float ratingOverall, float ratingDesign, float ratingNavigation, float ratingFunctionality, float ratingRecommendation, String feedback, EmailCallback callback) {
        new SendFeedbackTask(context, callback).execute(userEmail, userName, String.valueOf(ratingOverall), String.valueOf(ratingDesign), String.valueOf(ratingNavigation), String.valueOf(ratingFunctionality), String.valueOf(ratingRecommendation), feedback);
    }
    
    private static class SendEmailTask extends AsyncTask<String, Void, Boolean> {
        private Context context;
        private EmailCallback callback;
        private String errorMessage;
        private boolean emailSent = false;
        private Handler mainHandler;
        
        public SendEmailTask(Context context, EmailCallback callback) {
            this.context = context;
            this.callback = callback;
            this.mainHandler = new Handler(Looper.getMainLooper());
        }
        
        @Override
        protected Boolean doInBackground(String... params) {
            if (params.length < 2) {
                errorMessage = "Invalid parameters";
                return false;
            }
            
            String toEmail = params[0];
            String resetCode = params[1];
            
            try {
                return sendEmailViaWebView(toEmail, resetCode);
            } catch (Exception e) {
                errorMessage = e.getMessage();
                Log.e(TAG, "Error sending email: " + e.getMessage());
                return false;
            }
        }
        
        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                callback.onSuccess();
            } else {
                callback.onFailure(errorMessage != null ? errorMessage : "Failed to send email");
            }
        }
        
        private boolean sendEmailViaWebView(String toEmail, String resetCode) {
            // Create a hidden WebView to execute EmailJS
            mainHandler.post(() -> {
                WebView webView = new WebView(context);
                webView.getSettings().setJavaScriptEnabled(true);
                webView.getSettings().setDomStorageEnabled(true);
                webView.getSettings().setAllowFileAccess(true);
                webView.getSettings().setAllowContentAccess(true);
                
                // Add JavaScript interface for communication
                webView.addJavascriptInterface(new EmailJSInterface(), "Android");
                
                // Set WebViewClient to handle page load
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        // Inject EmailJS and send email
                        String emailJS = 
                            "var script = document.createElement('script');" +
                            "script.src = 'https://cdn.jsdelivr.net/npm/@emailjs/browser@3/dist/email.min.js';" +
                            "script.onload = function() {" +
                            "  emailjs.init('" + EMAILJS_PUBLIC_KEY + "');" +
                            "  emailjs.send('" + EMAILJS_SERVICE_ID + "', '" + PASSWORD_RESET_TEMPLATE_ID + "', {" +
                            "    to_email: '" + toEmail + "'," +
                            "    reset_code: '" + resetCode + "'," +
                            "    app_name: 'Campus Expense Manager'," +
                            "    expiry_time: '10 minutes'" +
                            "  }).then(" +
                            "    function(response) {" +
                            "      Android.onEmailSuccess();" +
                            "    }," +
                            "    function(error) {" +
                            "      Android.onEmailError(error.text || 'Failed to send email');" +
                            "    }" +
                            "  );" +
                            "};" +
                            "document.head.appendChild(script);";
                        
                        webView.evaluateJavascript(emailJS, null);
                    }
                });
                
                // Load a blank HTML page
                webView.loadData("<html><body></body></html>", "text/html", "UTF-8");
            });
            
            // Wait for result (with timeout)
            long startTime = System.currentTimeMillis();
            while (!emailSent && (System.currentTimeMillis() - startTime) < 30000) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
            
            return emailSent;
        }
        
        // JavaScript interface for communication between WebView and Android
        private class EmailJSInterface {
            @JavascriptInterface
            public void onEmailSuccess() {
                Log.d(TAG, "Email sent successfully via EmailJS WebView");
                emailSent = true;
            }
            
            @JavascriptInterface
            public void onEmailError(String error) {
                Log.e(TAG, "EmailJS WebView error: " + error);
                errorMessage = error;
                emailSent = false;
            }
        }
    }
    
    private static class SendFeedbackTask extends AsyncTask<String, Void, Boolean> {
        private Context context;
        private EmailCallback callback;
        private String errorMessage;
        private boolean emailSent = false;
        private Handler mainHandler;
        
        public SendFeedbackTask(Context context, EmailCallback callback) {
            this.context = context;
            this.callback = callback;
            this.mainHandler = new Handler(Looper.getMainLooper());
        }
        
        @Override
        protected Boolean doInBackground(String... params) {
            if (params.length < 8) {
                errorMessage = "Invalid parameters";
                return false;
            }
            
            String userEmail = params[0];
            String userName = params[1];
            String ratingOverall = params[2];
            String ratingDesign = params[3];
            String ratingNavigation = params[4];
            String ratingFunctionality = params[5];
            String ratingRecommendation = params[6];
            String feedback = params[7];
            
            try {
                return sendFeedbackViaWebView(userEmail, userName, ratingOverall, ratingDesign, ratingNavigation, ratingFunctionality, ratingRecommendation, feedback);
            } catch (Exception e) {
                errorMessage = e.getMessage();
                Log.e(TAG, "Error sending feedback email: " + e.getMessage());
                return false;
            }
        }
        
        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                callback.onSuccess();
            } else {
                callback.onFailure(errorMessage != null ? errorMessage : "Failed to send feedback");
            }
        }
        
        private boolean sendFeedbackViaWebView(String userEmail, String userName, String ratingOverall, String ratingDesign, String ratingNavigation, String ratingFunctionality, String ratingRecommendation, String feedback) {
            mainHandler.post(() -> {
                WebView webView = new WebView(context);
                webView.getSettings().setJavaScriptEnabled(true);
                webView.getSettings().setDomStorageEnabled(true);
                webView.getSettings().setAllowFileAccess(true);
                webView.getSettings().setAllowContentAccess(true);
                
                webView.addJavascriptInterface(new FeedbackEmailJSInterface(), "Android");
                
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        String emailJS = 
                            "var script = document.createElement('script');" +
                            "script.src = 'https://cdn.jsdelivr.net/npm/@emailjs/browser@3/dist/email.min.js';" +
                            "script.onload = function() {" +
                            "  emailjs.init('" + EMAILJS_PUBLIC_KEY + "');" +
                            "  emailjs.send('" + EMAILJS_SERVICE_ID + "', '" + FEEDBACK_TEMPLATE_ID + "', {" +
                            "    to_email: 'duquanghuy@gmail.com'," +
                            "    from_name: '" + userName.replace("'", "\\'") + "'," +
                            "    from_email: '" + userEmail + "'," +
                            "    rating_overall: '" + ratingOverall + "'," +
                            "    rating_design: '" + ratingDesign + "'," +
                            "    rating_navigation: '" + ratingNavigation + "'," +
                            "    rating_functionality: '" + ratingFunctionality + "'," +
                            "    rating_recommendation: '" + ratingRecommendation + "'," +
                            "    feedback: '" + feedback.replace("'", "\\'") + "'," +
                            "    app_name: 'Campus Expense Manager'" +
                            "  }).then(" +
                            "    function(response) {" +
                            "      Android.onEmailSuccess();" +
                            "    }," +
                            "    function(error) {" +
                            "      Android.onEmailError(error.text || 'Failed to send feedback');" +
                            "    }" +
                            "  );" +
                            "};" +
                            "document.head.appendChild(script);";
                        
                        webView.evaluateJavascript(emailJS, null);
                    }
                });
                
                webView.loadData("<html><body></body></html>", "text/html", "UTF-8");
            });
            
            long startTime = System.currentTimeMillis();
            while (!emailSent && (System.currentTimeMillis() - startTime) < 30000) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
            
            return emailSent;
        }
        
        private class FeedbackEmailJSInterface {
            @JavascriptInterface
            public void onEmailSuccess() {
                Log.d(TAG, "Feedback email sent successfully via EmailJS WebView");
                emailSent = true;
            }
            
            @JavascriptInterface
            public void onEmailError(String error) {
                Log.e(TAG, "Feedback EmailJS WebView error: " + error);
                errorMessage = error;
                emailSent = false;
            }
        }
    }
} 