package com.demo.copilot.taskmanager.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis cache configuration with security considerations.
 * 
 * Features:
 * - TTL-based cache expiration
 * - JSON serialization for complex objects
 * - Different cache configurations per cache name
 * - Fallback to simple cache when Redis is not available
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Primary Redis cache manager when Redis is available.
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = false)
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        
        // Default cache configuration with enhanced features
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10)) // Default TTL: 10 minutes
                .prefixCacheNameWith("taskmanager:") // Prevent key conflicts
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues(); // Security: don't cache null values
        
        // Specific cache configurations
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // User cache - longer TTL for user data
        cacheConfigurations.put("users", defaultCacheConfig
                .entryTtl(Duration.ofMinutes(30)));
        
        // Task cache - shorter TTL for frequently changing data
        cacheConfigurations.put("tasks", defaultCacheConfig
                .entryTtl(Duration.ofMinutes(5)));
        
        // Project cache - medium TTL
        cacheConfigurations.put("projects", defaultCacheConfig
                .entryTtl(Duration.ofMinutes(15)));
        
        // Authentication cache - very short TTL for security
        cacheConfigurations.put("auth", defaultCacheConfig
                .entryTtl(Duration.ofMinutes(2)));
        
        // JWT blacklist cache - TTL based on token expiration
        cacheConfigurations.put("jwt-blacklist", defaultCacheConfig
                .entryTtl(Duration.ofHours(24)));
        
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}