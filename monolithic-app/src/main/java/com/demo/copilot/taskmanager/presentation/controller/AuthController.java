package com.demo.copilot.taskmanager.presentation.controller;

import com.demo.copilot.taskmanager.application.dto.user.CreateUserRequest;
import com.demo.copilot.taskmanager.application.dto.user.UserResponse;
import com.demo.copilot.taskmanager.application.service.UserService;
import com.demo.copilot.taskmanager.infrastructure.security.JwtService;
import com.demo.copilot.taskmanager.presentation.dto.request.LoginRequest;
import com.demo.copilot.taskmanager.presentation.dto.response.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication operations.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "User authentication and registration")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(UserService userService, 
                         AuthenticationManager authenticationManager,
                         JwtService jwtService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody CreateUserRequest request) {
        UserResponse user = userService.createUser(request);
        
        // Generate JWT token for the new user
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);
        
        AuthResponse response = AuthResponse.builder()
            .token(token)
            .user(user)
            .message("User registered successfully")
            .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and return JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);
        
        UserResponse user = userService.getUserByEmail(request.getEmail());
        
        AuthResponse response = AuthResponse.builder()
            .token(token)
            .user(user)
            .message("Login successful")
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/logout")
    @Operation(summary = "Logout user and blacklist JWT token")
    public ResponseEntity<Void> logout(jakarta.servlet.http.HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            jwtService.blacklistToken(token);
        }
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT token")
    public ResponseEntity<AuthResponse> refresh(jakarta.servlet.http.HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().build();
        }
        
        String refreshToken = authHeader.substring(7);
        
        if (!jwtService.isRefreshToken(refreshToken) || !jwtService.validateToken(refreshToken)) {
            return ResponseEntity.badRequest().build();
        }
        
        String username = jwtService.extractUsername(refreshToken);
        UserResponse user = userService.getUserByUsername(username);
        
        // Create a simple UserDetails implementation
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
            .username(user.getUsername())
            .password("") // Not needed for token generation
            .authorities("ROLE_" + user.getRole().name())
            .build();
        
        String newToken = jwtService.generateToken(userDetails);
        
        AuthResponse response = AuthResponse.builder()
            .token(newToken)
            .user(user)
            .message("Token refreshed successfully")
            .build();
        
        return ResponseEntity.ok(response);
    }
}