package com.demo.copilot.taskmanager.infrastructure.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Enhanced JWT token service for generating and validating JWT tokens with improved security.
 * 
 * Security improvements:
 * - Proper key generation and handling with minimum length requirements
 * - Enhanced JWT claims validation including issuer, audience, and not-before
 * - Token blacklisting capability for secure logout
 * - Refresh token mechanism
 * - Comprehensive logging for security events
 */
@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
    
    // Security constants
    private static final int MIN_SECRET_LENGTH = 32; // Minimum 256 bits for HS256
    private static final String TOKEN_TYPE = "JWT";
    private static final String ISSUER = "taskmanager-api";
    private static final String AUDIENCE = "taskmanager-clients";
    
    @Value("${spring.security.jwt.secret}")
    private String jwtSecret;

    @Value("${spring.security.jwt.expiration}")
    private long jwtExpiration;
    
    @Value("${spring.security.jwt.refresh-expiration}")
    private long refreshTokenExpiration;

    /**
     * Extract username from JWT token with enhanced validation.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract expiration date from JWT token.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    /**
     * Extract issued at date from JWT token.
     */
    public Date extractIssuedAt(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }
    
    /**
     * Extract not before date from JWT token.
     */
    public Date extractNotBefore(String token) {
        return extractClaim(token, Claims::getNotBefore);
    }
    
    /**
     * Extract JWT ID from token.
     */
    public String extractJwtId(String token) {
        return extractClaim(token, Claims::getId);
    }
    
    /**
     * Extract issuer from JWT token.
     */
    public String extractIssuer(String token) {
        return extractClaim(token, Claims::getIssuer);
    }
    
    /**
     * Extract audience from JWT token.
     */
    public String extractAudience(String token) {
        return extractClaim(token, claims -> claims.get("aud", String.class));
    }

    /**
     * Extract claim from JWT token using claims resolver.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from JWT token with enhanced error handling.
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSignInKey())
                    .requireIssuer(ISSUER)
                    .requireAudience(AUDIENCE)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token is expired: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            logger.error("JWT token is malformed: {}", e.getMessage());
            throw e;
        } catch (SecurityException e) {
            logger.error("JWT signature validation failed: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Check if token is expired.
     */
    private Boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        return expiration.before(new Date());
    }
    
    /**
     * Check if token is not yet valid (before not-before time).
     */
    private Boolean isTokenNotYetValid(String token) {
        try {
            Date notBefore = extractNotBefore(token);
            return notBefore != null && notBefore.after(new Date());
        } catch (Exception e) {
            // If no not-before claim, token is considered valid
            return false;
        }
    }

    /**
     * Generate token with default claims.
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generate token with extra claims.
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }
    
    /**
     * Generate refresh token.
     */
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> refreshClaims = new HashMap<>();
        refreshClaims.put("type", "refresh");
        return buildToken(refreshClaims, userDetails, refreshTokenExpiration);
    }

    /**
     * Build JWT token with enhanced claims and security.
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        String jwtId = UUID.randomUUID().toString();
        
        // Add authorities as claims
        List<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        
        Map<String, Object> claims = new HashMap<>(extraClaims);
        claims.put("authorities", authorities);
        claims.put("aud", AUDIENCE);
        
        logger.debug("Generating JWT for user: {} with authorities: {}", userDetails.getUsername(), authorities);
        
        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuer(ISSUER)
                .issuedAt(now)
                .notBefore(now)
                .expiration(expiryDate)
                .id(jwtId)
                .header()
                    .type(TOKEN_TYPE)
                    .and()
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Validate token against user details with comprehensive checks.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            boolean isUsernameValid = username.equals(userDetails.getUsername());
            boolean isNotExpired = !isTokenExpired(token);
            boolean isNotTooEarly = !isTokenNotYetValid(token);
            boolean isNotBlacklisted = !isTokenBlacklisted(token);
            
            boolean isValid = isUsernameValid && isNotExpired && isNotTooEarly && isNotBlacklisted;
            
            if (!isValid) {
                logger.warn("Token validation failed for user: {}. Username valid: {}, Not expired: {}, Not too early: {}, Not blacklisted: {}", 
                    username, isUsernameValid, isNotExpired, isNotTooEarly, isNotBlacklisted);
            }
            
            return isValid;
        } catch (Exception e) {
            logger.error("Token validation error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validate token structure and claims.
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            
            // Validate required claims
            if (claims.getSubject() == null || claims.getSubject().trim().isEmpty()) {
                logger.warn("JWT token missing or empty subject");
                return false;
            }
            
            if (claims.getIssuedAt() == null) {
                logger.warn("JWT token missing issued at claim");
                return false;
            }
            
            if (claims.getExpiration() == null) {
                logger.warn("JWT token missing expiration claim");
                return false;
            }
            
            // Check if token is blacklisted
            if (isTokenBlacklisted(token)) {
                logger.warn("JWT token is blacklisted");
                return false;
            }
            
            return true;
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token is expired: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            logger.error("JWT token is malformed: {}", e.getMessage());
            return false;
        } catch (SecurityException e) {
            logger.error("JWT signature validation failed: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("JWT validation error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get username from token (alias for extractUsername).
     */
    public String getUsernameFromToken(String token) {
        return extractUsername(token);
    }
    
    /**
     * Check if token is a refresh token.
     */
    public boolean isRefreshToken(String token) {
        try {
            String tokenType = extractClaim(token, claims -> claims.get("type", String.class));
            return "refresh".equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Blacklist a token (add to blacklist).
     */
    public void blacklistToken(String token) {
        try {
            String jwtId = extractJwtId(token);
            Date expiration = extractExpiration(token);
            blacklistedTokens.put(jwtId, expiration);
            logger.info("Token blacklisted with ID: {}", jwtId);
        } catch (Exception e) {
            logger.error("Failed to blacklist token: {}", e.getMessage());
        }
    }
    
    /**
     * Check if token is blacklisted.
     */
    public boolean isTokenBlacklisted(String token) {
        try {
            String jwtId = extractJwtId(token);
            if (jwtId == null) {
                return false;
            }
            
            Date expiration = blacklistedTokens.get(jwtId);
            if (expiration == null) {
                return false;
            }
            
            // Remove expired blacklisted tokens
            if (expiration.before(new Date())) {
                blacklistedTokens.remove(jwtId);
                return false;
            }
            
            return true;
        } catch (Exception e) {
            logger.error("Error checking token blacklist status: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Clean up expired blacklisted tokens.
     */
    public void cleanupExpiredBlacklistedTokens() {
        Date now = new Date();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().before(now));
        logger.debug("Cleaned up expired blacklisted tokens");
    }

    /**
     * Get secure signing key with enhanced validation.
     */
    private SecretKey getSignInKey() {
        validateSecretKey();
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * Validate that the secret key meets security requirements.
     */
    private void validateSecretKey() {
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalStateException("JWT secret cannot be null or empty");
        }
        
        if (jwtSecret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                String.format("JWT secret must be at least %d bytes long for security", MIN_SECRET_LENGTH)
            );
        }
        
        // Warn if using default/weak secret
        if (jwtSecret.contains("demo") || jwtSecret.contains("secret") || jwtSecret.contains("test")) {
            logger.warn("SECURITY WARNING: Using a weak or default JWT secret. Please use a strong, randomly generated secret in production!");
        }
    }
    
    // In-memory token blacklist (in production, use Redis or database)
    private final Map<String, Date> blacklistedTokens = new HashMap<>();
}