package com.demo.copilot.taskmanager.gateway.config;

import com.demo.copilot.taskmanager.gateway.config.properties.GatewayProperties;
import com.demo.copilot.taskmanager.gateway.config.properties.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.security.Principal;

/**
 * Gateway configuration for rate limiting, fallbacks, and other cross-cutting concerns.
 * 
 * Uses modern Spring Boot 3.x @ConfigurationProperties with record types
 * for type-safe, validated configuration management.
 */
@Configuration
@EnableConfigurationProperties({GatewayProperties.class, SecurityProperties.class})
public class GatewayConfig {

    /**
     * Key resolver for rate limiting based on user identity.
     * Falls back to IP address if user is not authenticated.
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> exchange.getPrincipal()
                .cast(Principal.class)
                .map(Principal::getName)
                .switchIfEmpty(Mono.fromCallable(() -> 
                    exchange.getRequest().getRemoteAddress() != null 
                        ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                        : "unknown"));
    }

    /**
     * Fallback routes for circuit breaker patterns.
     */
    @Bean
    public RouterFunction<ServerResponse> fallbackRoutes() {
        return RouterFunctions
            .route()
            .GET("/fallback/user-service", request ->
                ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue("{\"error\":\"User service is temporarily unavailable. Please try again later.\"}")))
            .GET("/fallback/task-service", request ->
                ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue("{\"error\":\"Task service is temporarily unavailable. Please try again later.\"}")))
            .GET("/fallback/notification-service", request ->
                ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue("{\"error\":\"Notification service is temporarily unavailable. Please try again later.\"}")))
            .POST("/fallback/user-service", request ->
                ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue("{\"error\":\"User service is temporarily unavailable. Please try again later.\"}")))
            .POST("/fallback/task-service", request ->
                ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue("{\"error\":\"Task service is temporarily unavailable. Please try again later.\"}")))
            .POST("/fallback/notification-service", request ->
                ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue("{\"error\":\"Notification service is temporarily unavailable. Please try again later.\"}")))
            .build();
    }
}