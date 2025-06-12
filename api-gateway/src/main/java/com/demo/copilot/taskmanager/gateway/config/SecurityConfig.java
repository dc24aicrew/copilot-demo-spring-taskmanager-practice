package com.demo.copilot.taskmanager.gateway.config;

import com.demo.copilot.taskmanager.gateway.config.properties.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.config.Customizer;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Modern Spring Security 6.x configuration for API Gateway.
 * 
 * Implements OAuth2 Resource Server patterns with ReactiveJwtDecoder
 * for validating JWT tokens in a WebFlux reactive environment.
 * 
 * This replaces the previous manual JWT configuration with Spring Security's
 * built-in OAuth2 Resource Server support for better security and maintainability.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final SecurityProperties securityProperties;

    public SecurityConfig(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    /**
     * Configures the main security filter chain for the API Gateway.
     * 
     * Uses modern Spring Security 6.x OAuth2 Resource Server configuration
     * with ReactiveJwtDecoder for JWT validation in WebFlux environment.
     */
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
            .authorizeExchange(exchanges -> exchanges
                // Allow health checks and actuator endpoints without authentication
                .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                // Allow fallback endpoints for circuit breaker patterns
                .pathMatchers("/fallback/**").permitAll()
                // Require authentication for all other requests
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(Customizer.withDefaults())
            )
            // Disable CSRF for stateless JWT authentication
            .csrf(csrf -> csrf.disable())
            // Disable form login as we use JWT
            .formLogin(form -> form.disable())
            // Disable HTTP Basic as we use JWT
            .httpBasic(basic -> basic.disable())
            .build();
    }

    /**
     * Configures ReactiveJwtDecoder for validating JWT tokens.
     * 
     * Uses symmetric key (HMAC-SHA256) for demonstration purposes.
     * In production, consider using RSA public keys from a JWK Set URI
     * or configuring with an external authorization server.
     */
    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        // For demo purposes, using symmetric key with HMAC-SHA256
        // In production, use RSA keys or JWK Set URI from authorization server
        SecretKeySpec secretKey = new SecretKeySpec(
            securityProperties.jwtSecret().getBytes(StandardCharsets.UTF_8), 
            "HmacSHA256"
        );
        
        return NimbusReactiveJwtDecoder.withSecretKey(secretKey).build();
        
        // Alternative for production with external auth server:
        // return ReactiveJwtDecoders.fromIssuerLocation("https://your-auth-server.com");
        
        // Alternative for JWK Set URI:
        // return NimbusReactiveJwtDecoder.withJwkSetUri("https://your-auth-server.com/.well-known/jwks.json").build();
    }
}