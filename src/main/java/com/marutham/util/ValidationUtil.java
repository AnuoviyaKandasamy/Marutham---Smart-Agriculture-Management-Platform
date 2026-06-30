package com.marutham.util;

import java.util.regex.Pattern;


public class ValidationUtil {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");
    private static final Pattern XSS_PATTERN = Pattern.compile("<[^>]*>|javascript:|onerror=|onclick=", Pattern.CASE_INSENSITIVE);


    public static ValidationResult validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return new ValidationResult(false, "Username cannot be empty");
        }
        if (username.length() < 3 || username.length() > 20) {
            return new ValidationResult(false, "Username must be 3-20 characters");
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return new ValidationResult(false, "Username can only contain letters, numbers, and underscores");
        }
        return new ValidationResult(true, "Valid");
    }


    public static ValidationResult validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return new ValidationResult(false, "Email cannot be empty");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return new ValidationResult(false, "Invalid email format");
        }
        if (email.length() > 100) {
            return new ValidationResult(false, "Email is too long");
        }
        return new ValidationResult(true, "Valid");
    }


    public static ValidationResult validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return new ValidationResult(false, "Password cannot be empty");
        }
        if (password.length() < 8) {
            return new ValidationResult(false, "Password must be at least 8 characters");
        }
        if (password.length() > 128) {
            return new ValidationResult(false, "Password is too long");
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            return new ValidationResult(false, "Password must contain uppercase, lowercase, and numbers");
        }
        return new ValidationResult(true, "Valid");
    }


    public static ValidationResult validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return new ValidationResult(false, "Phone cannot be empty");
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            return new ValidationResult(false, "Phone must be 10 digits");
        }
        return new ValidationResult(true, "Valid");
    }


    public static ValidationResult validatePostContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return new ValidationResult(false, "Content cannot be empty");
        }
        if (content.length() > 5000) {
            return new ValidationResult(false, "Content cannot exceed 5000 characters");
        }
        if (containsXSS(content)) {
            return new ValidationResult(false, "Content contains invalid HTML/scripts");
        }
        return new ValidationResult(true, "Valid");
    }

    public static ValidationResult validatePositiveInteger(String value) {
        if (value == null || value.isEmpty()) {
            return new ValidationResult(false, "Value cannot be empty");
        }
        try {
            int intValue = Integer.parseInt(value);
            if (intValue <= 0) {
                return new ValidationResult(false, "Value must be positive");
            }
            return new ValidationResult(true, "Valid");
        } catch (NumberFormatException e) {
            return new ValidationResult(false, "Value must be a valid number");
        }
    }


    public static boolean containsXSS(String text) {
        if (text == null) return false;
        return XSS_PATTERN.matcher(text).find();
    }

    public static String sanitizeForDisplay(String input) {
        if (input == null) return "";
        return input
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&#x27;")
                .replaceAll("/", "&#x2F;");
    }


    public static String sanitizeInput(String input) {
        if (input == null) return "";
        return input.trim();
    }


    public static class ValidationResult {
        public boolean isValid;
        public String message;

        public ValidationResult(boolean isValid, String message) {
            this.isValid = isValid;
            this.message = message;
        }
    }
}
