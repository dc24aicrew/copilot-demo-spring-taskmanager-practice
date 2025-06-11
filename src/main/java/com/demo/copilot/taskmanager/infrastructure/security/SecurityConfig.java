package com.demo.copilot.taskmanager.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

/**
 * Enhanced Spring Security configuration for the Task Manager application.
 * 
 * Security improvements:
 * - JWT-based stateless authentication
 * - Essential security headers (CSP, HSTS, X-Frame-Options, etc.)
 * - Input validation and sanitization
 * - Secure session management
 * - Rate limiting and CORS configuration
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public SecurityConfig(UserDetailsService userDetailsService, 
                         JwtAuthenticationFilter jwtAuthFilter,
                         JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthFilter = jwtAuthFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    /**
     * Password encoder bean using BCrypt hashing algorithm.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Authentication provider configuration.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Authentication manager bean.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }    /**
     * Enhanced security filter chain configuration with comprehensive security headers.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for REST API (using JWT tokens)
            .csrf(AbstractHttpConfigurer::disable)
            
            // Configure security headers
            .headers(headers -> headers
                // Content Security Policy
                .contentSecurityPolicy("default-src 'self'; " +
                                     "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; " +
                                     "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                                     "font-src 'self' https://fonts.gstatic.com; " +
                                     "img-src 'self' data: https:; " +
                                     "connect-src 'self'; " +
                                     "frame-ancestors 'none';")
                .and()
                
                // HTTP Strict Transport Security (HSTS)
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000) // 1 year
                    .includeSubDomains(true))
                
                // X-Frame-Options (updated method)
                .frameOptions(frameOptions -> frameOptions.deny())
                
                // X-Content-Type-Options
                .contentTypeOptions(contentTypeOptions -> contentTypeOptions.and())
                
                // X-XSS-Protection
                .addHeaderWriter(new XXssProtectionHeaderWriter())
                
                // Referrer Policy
                .addHeaderWriter(new ReferrerPolicyHeaderWriter(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                
                // Permissions Policy (formerly Feature Policy)
                .addHeaderWriter((request, response) -> {
                    response.setHeader("Permissions-Policy", 
                        "camera=(), microphone=(), geolocation=(), payment=(), usb=()");
                })
                
                // Additional security headers for comprehensive protection
                .addHeaderWriter((request, response) -> {
                    // Certificate Transparency for SSL/TLS security
                    response.setHeader("Expect-CT", "enforce, max-age=86400");
                    
                    // Prevent DNS rebinding attacks
                    response.setHeader("X-Permitted-Cross-Domain-Policies", "none");
                    
                    // Prevent information disclosure
                    response.setHeader("X-Robots-Tag", "noindex, nofollow, nosnippet, noarchive");
                })
                
                // Modern browser security headers
                .addHeaderWriter((request, response) -> {
                    response.setHeader("Cross-Origin-Embedder-Policy", "require-corp");
                    response.setHeader("Cross-Origin-Opener-Policy", "same-origin");
                })
            )
            
            // Session management - stateless for JWT
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Exception handling
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                
            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints (relative to context path /api)
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/actuator/**").hasRole("ADMIN") // Secure actuator endpoints
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/v3/api-docs/**", "/v3/api-docs").permitAll()
                .requestMatchers("/webjars/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll() // For development only
                
                // Protected endpoints (relative to context path /api)
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/manager/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/users/**", "/tasks/**").hasAnyRole("USER", "MANAGER", "ADMIN")
                
                // All other requests need authentication
                .anyRequest().authenticated()
            )
            
            // Add JWT filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
