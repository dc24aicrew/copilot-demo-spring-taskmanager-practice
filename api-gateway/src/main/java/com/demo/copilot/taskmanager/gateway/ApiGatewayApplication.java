package com.demo.copilot.taskmanager.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway application for the Task Manager microservices architecture.
 * 
 * This gateway serves as the single entry point for all client requests and provides:
 * 
 * Key Features:
 * - Request routing to appropriate microservices
 * - Load balancing across service instances
 * - Rate limiting and throttling
 * - Authentication and authorization
 * - Request/response transformation
 * - Circuit breaker patterns for resilience
 * - CORS handling
 * - Request logging and monitoring
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}