package com.demo.copilot.taskmanager.gateway.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Duration;

/**
 * Security configuration properties using modern Spring Boot 3.x patterns.
 * 
 * Uses record type for immutable configuration with proper validation.
 * Replaces hardcoded security values with externalized configuration.
 */
@ConfigurationProperties(prefix = "app.security")
@Validated
public record SecurityProperties(
    @NotBlank 
    @Size(min = 32, message = "JWT secret must be at least 32 characters for security")
    String jwtSecret,
    
    Duration jwtExpiration,
    Duration refreshExpiration
) {
    /**
     * Default constructor with sensible defaults for development.
     * Production deployments should override these values.
     */
    public SecurityProperties() {
        this(
            "demo-secret-key-change-in-production-minimum-32-characters", // Default for demo
            Duration.ofHours(1), // 1 hour JWT expiration
            Duration.ofDays(7)   // 7 days refresh token expiration
        );
    }
}