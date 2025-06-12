package com.demo.copilot.taskmanager.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Server application for service discovery in the Task Manager microservices architecture.
 * 
 * This server acts as a service registry where all microservices register themselves
 * and discover other services for inter-service communication.
 * 
 * Key Features:
 * - Service registration and discovery
 * - Health monitoring of registered services
 * - Load balancing support
 * - Fault tolerance with replication support
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}