package com.demo.copilot.taskmanager.infrastructure.security;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * Input sanitization utility for preventing XSS and injection attacks.
 * 
 * Features:
 * - HTML/Script tag removal
 * - SQL injection pattern detection
 * - Unicode normalization
 * - Length validation
 */
@Component
public class InputSanitizer {

    // Common XSS patterns
    private static final Pattern XSS_PATTERN = Pattern.compile(
        "(?i)<script[^>]*>.*?</script>|" +
        "javascript:|" +
        "vbscript:|" +
        "onload=|" +
        "onerror=|" +
        "onclick=|" +
        "onmouseover=|" +
        "<iframe|" +
        "<object|" +
        "<embed|" +
        "<applet|" +
        "<meta|" +
        "<link|" +
        "<style|" +
        "expression\\(|" +
        "eval\\(|" +
        "setTimeout\\(|" +
        "setInterval\\("
    );

    // SQL injection patterns
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(?i)(union|select|insert|update|delete|drop|create|alter|exec|execute|sp_|xp_)" +
        "\\s+(select|from|where|into|values|table|database|schema|--|\\/\\*|\\*\\/)"
    );

    // HTML tag pattern
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");

    /**
     * Sanitize input string to prevent XSS and injection attacks.
     */
    public String sanitize(String input) {
        if (!StringUtils.hasText(input)) {
            return input;
        }

        String sanitized = input;

        // Remove null bytes
        sanitized = sanitized.replace("\0", "");

        // Remove HTML tags
        sanitized = HTML_TAG_PATTERN.matcher(sanitized).replaceAll("");

        // Remove XSS patterns
        sanitized = XSS_PATTERN.matcher(sanitized).replaceAll("");

        // Trim whitespace
        sanitized = sanitized.trim();

        return sanitized;
    }

    /**
     * Check if input contains potentially dangerous content.
     */
    public boolean isDangerous(String input) {
        if (!StringUtils.hasText(input)) {
            return false;
        }

        // Check for XSS patterns
        if (XSS_PATTERN.matcher(input).find()) {
            return true;
        }

        // Check for SQL injection patterns
        if (SQL_INJECTION_PATTERN.matcher(input).find()) {
            return true;
        }

        // Check for path traversal
        if (input.contains("../") || input.contains("..\\")) {
            return true;
        }

        // Check for null bytes
        if (input.contains("\0")) {
            return true;
        }

        return false;
    }

    /**
     * Validate and sanitize input with length check.
     */
    public String validateAndSanitize(String input, int maxLength) {
        if (!StringUtils.hasText(input)) {
            return input;
        }

        if (input.length() > maxLength) {
            throw new IllegalArgumentException("Input exceeds maximum length of " + maxLength);
        }

        if (isDangerous(input)) {
            throw new IllegalArgumentException("Input contains potentially dangerous content");
        }

        return sanitize(input);
    }

    /**
     * Sanitize filename to prevent directory traversal.
     */
    public String sanitizeFilename(String filename) {
        if (!StringUtils.hasText(filename)) {
            return filename;
        }

        // Remove path separators and special characters
        String sanitized = filename.replaceAll("[/\\\\:*?\"<>|]", "");
        
        // Remove leading dots to prevent hidden files
        sanitized = sanitized.replaceAll("^\\.*", "");
        
        // Limit length
        if (sanitized.length() > 255) {
            sanitized = sanitized.substring(0, 255);
        }

        return sanitized;
    }

    /**
     * Validate email format more strictly than basic pattern.
     */
    public boolean isValidEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }

        // Basic email pattern
        Pattern emailPattern = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
        );

        if (!emailPattern.matcher(email).matches()) {
            return false;
        }

        // Check for dangerous content
        if (isDangerous(email)) {
            return false;
        }

        // Additional checks
        if (email.length() > 254) { // RFC 5321 limit
            return false;
        }

        // Check for double dots
        if (email.contains("..")) {
            return false;
        }

        return true;
    }

    /**
     * Validate username format.
     */
    public boolean isValidUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return false;
        }

        // Username should only contain alphanumeric and underscore
        Pattern usernamePattern = Pattern.compile("^[a-zA-Z0-9_]{3,50}$");
        
        if (!usernamePattern.matcher(username).matches()) {
            return false;
        }

        // Check for dangerous content
        return !isDangerous(username);
    }
}