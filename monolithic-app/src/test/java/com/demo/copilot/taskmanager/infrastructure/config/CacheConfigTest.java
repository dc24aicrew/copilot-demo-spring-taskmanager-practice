package com.demo.copilot.taskmanager.infrastructure.config;

import com.demo.copilot.taskmanager.application.dto.user.UserResponse;
import com.demo.copilot.taskmanager.domain.valueobject.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for Redis cache configuration with LocalDateTime serialization.
 */
@SpringBootTest
@ActiveProfiles("test")
class CacheConfigTest {

    @Autowired(required = false)
    private CacheManager cacheManager;

    @Test
    void testLocalDateTimeSerializationInCache() {
        // Skip test if Redis cache is not available (test profile typically uses in-memory cache)
        if (cacheManager == null || !cacheManager.getClass().getSimpleName().contains("Redis")) {
            return;
        }

        // Create a UserResponse with LocalDateTime fields
        LocalDateTime now = LocalDateTime.now();
        UserResponse userResponse = new UserResponse(
                UUID.randomUUID(),
                "testuser",
                "test@example.com",
                "Test",
                "User",
                UserRole.USER,
                true,
                "avatar.jpg",
                now,
                now.minusDays(1),
                now
        );

        // Get the users cache
        Cache usersCache = cacheManager.getCache("users");
        assertThat(usersCache).isNotNull();

        // Put the UserResponse in cache - this should not throw a serialization exception
        usersCache.put("test-user", userResponse);

        // Retrieve from cache
        Cache.ValueWrapper cached = usersCache.get("test-user");
        assertThat(cached).isNotNull();

        UserResponse retrievedUser = (UserResponse) cached.get();
        assertThat(retrievedUser).isNotNull();
        assertThat(retrievedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(retrievedUser.getCreatedAt()).isNotNull();
        assertThat(retrievedUser.getUpdatedAt()).isNotNull();
        assertThat(retrievedUser.getLastLoginAt()).isNotNull();
    }

    @Test
    void testObjectMapperWithJavaTimeModule() {
        // Test that we can serialize/deserialize LocalDateTime with ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // This should include JSR310 module

        LocalDateTime now = LocalDateTime.now();
        UserResponse userResponse = new UserResponse(
                UUID.randomUUID(),
                "testuser",
                "test@example.com",
                "Test",
                "User",
                UserRole.USER,
                true,
                "avatar.jpg",
                now,
                now.minusDays(1),
                now
        );

        // This should not throw an exception
        try {
            String json = objectMapper.writeValueAsString(userResponse);
            assertThat(json).contains("createdAt");
            assertThat(json).contains("updatedAt");
            assertThat(json).contains("lastLoginAt");

            UserResponse deserialized = objectMapper.readValue(json, UserResponse.class);
            assertThat(deserialized.getEmail()).isEqualTo("test@example.com");
        } catch (Exception e) {
            throw new AssertionError("LocalDateTime serialization failed", e);
        }
    }
}