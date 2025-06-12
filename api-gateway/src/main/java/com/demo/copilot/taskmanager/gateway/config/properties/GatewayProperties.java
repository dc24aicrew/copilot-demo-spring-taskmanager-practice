package com.demo.copilot.taskmanager.gateway.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;

/**
 * Configuration properties for Spring Cloud Gateway using modern Spring Boot 3.x patterns.
 * 
 * Uses record types for immutable configuration with constructor binding.
 * Includes JSR-303 validation to ensure configuration integrity.
 */
@ConfigurationProperties(prefix = "app.gateway")
@Validated
public record GatewayProperties(
    @Valid @NotNull RateLimiting rateLimiting,
    @Valid @NotNull CircuitBreaker circuitBreaker,
    @Valid @NotNull Retry retry
) {

    /**
     * Rate limiting configuration for API Gateway.
     */
    public record RateLimiting(
        @Min(1) @Max(1000) int replenishRate,
        @Min(1) @Max(2000) int burstCapacity,
        @Min(1) @Max(10) int requestedTokens
    ) {
        public RateLimiting() {
            this(10, 20, 1); // Default values
        }
    }

    /**
     * Circuit breaker configuration using Resilience4j patterns.
     */
    public record CircuitBreaker(
        @Min(5) @Max(100) int slidingWindowSize,
        @Min(1) @Max(100) int minimumNumberOfCalls,
        @Min(1) @Max(100) int failureRateThreshold,
        @NotNull Duration waitDurationInOpenState
    ) {
        public CircuitBreaker() {
            this(10, 5, 50, Duration.ofSeconds(30)); // Default values
        }
    }

    /**
     * Retry configuration for failed requests.
     */
    public record Retry(
        @Min(1) @Max(10) int maxAttempts,
        @NotNull Duration backoffDelay,
        @NotNull Duration maxBackoffDelay
    ) {
        public Retry() {
            this(3, Duration.ofMillis(100), Duration.ofSeconds(1)); // Default values
        }
    }
}