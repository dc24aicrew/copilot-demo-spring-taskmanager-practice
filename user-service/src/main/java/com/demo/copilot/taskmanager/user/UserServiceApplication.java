package com.demo.copilot.taskmanager.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * User Service microservice application for user management and authentication.
 * 
 * This service handles all user-related operations including:
 * - User registration and authentication
 * - User profile management
 * - JWT token generation and validation
 * - User role and permission management
 * 
 * Key Features:
 * - RESTful API for user operations
 * - JWT-based authentication
 * - Caching for improved performance
 * - Service discovery with Eureka
 * - Centralized configuration
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableFeignClients
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}