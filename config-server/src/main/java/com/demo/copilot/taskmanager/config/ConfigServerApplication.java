package com.demo.copilot.taskmanager.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Configuration Server application for centralized configuration management.
 * 
 * This server provides centralized configuration management for all microservices
 * in the Task Manager architecture. It supports:
 * 
 * Key Features:
 * - Centralized configuration management
 * - Environment-specific configurations
 * - Dynamic configuration refresh
 * - Git repository support for configuration versioning
 * - Encryption/decryption support for sensitive data
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}