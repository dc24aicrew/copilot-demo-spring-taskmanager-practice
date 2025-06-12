package com.demo.copilot.taskmanager.user.infrastructure.persistence;

import com.demo.copilot.taskmanager.user.infrastructure.persistence.entity.UserJpaEntity;
import com.demo.copilot.taskmanager.user.infrastructure.persistence.repository.UserJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for UserJpaRepository using @DataJpaTest.
 * 
 * These tests validate the JPA repository functionality using
 * Testcontainers with PostgreSQL for realistic database testing.
 */
@DataJpaTest
@Testcontainers
@DisplayName("User JPA Repository")
class UserJpaRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserJpaRepository userRepository;

    @Nested
    @DisplayName("Basic CRUD Operations")
    class BasicCrudOperations {

        @Test
        @DisplayName("Should save and find user by ID")
        void shouldSaveAndFindUserById() {
            // Given
            UserJpaEntity user = createTestUserEntity();
            
            // When
            UserJpaEntity savedUser = userRepository.save(user);
            Optional<UserJpaEntity> foundUser = userRepository.findById(savedUser.getId());
            
            // Then
            assertThat(savedUser.getId()).isNotNull();
            assertThat(foundUser).isPresent();
            assertThat(foundUser.get().getUsername()).isEqualTo(user.getUsername());
            assertThat(foundUser.get().getEmail()).isEqualTo(user.getEmail());
        }

        @Test
        @DisplayName("Should find user by username")
        void shouldFindUserByUsername() {
            // Given
            UserJpaEntity user = createTestUserEntity();
            userRepository.save(user);
            entityManager.flush();
            
            // When
            Optional<UserJpaEntity> foundUser = userRepository.findByUsername("john.doe");
            
            // Then
            assertThat(foundUser).isPresent();
            assertThat(foundUser.get().getEmail()).isEqualTo("john.doe@example.com");
        }

        @Test
        @DisplayName("Should find user by email")
        void shouldFindUserByEmail() {
            // Given
            UserJpaEntity user = createTestUserEntity();
            userRepository.save(user);
            entityManager.flush();
            
            // When
            Optional<UserJpaEntity> foundUser = userRepository.findByEmail("john.doe@example.com");
            
            // Then
            assertThat(foundUser).isPresent();
            assertThat(foundUser.get().getUsername()).isEqualTo("john.doe");
        }

        @Test
        @DisplayName("Should delete user by ID")
        void shouldDeleteUserById() {
            // Given
            UserJpaEntity user = createTestUserEntity();
            UserJpaEntity savedUser = userRepository.save(user);
            UUID userId = savedUser.getId();
            
            // When
            userRepository.deleteById(userId);
            entityManager.flush();
            
            // Then
            Optional<UserJpaEntity> foundUser = userRepository.findById(userId);
            assertThat(foundUser).isEmpty();
        }
    }

    @Nested
    @DisplayName("Query Methods")
    class QueryMethods {

        @Test
        @DisplayName("Should find active users only")
        void shouldFindActiveUsersOnly() {
            // Given
            UserJpaEntity activeUser = createTestUserEntity();
            activeUser.setUsername("active.user");
            activeUser.setEmail("active@example.com");
            activeUser.setIsActive(true);
            
            UserJpaEntity inactiveUser = createTestUserEntity();
            inactiveUser.setUsername("inactive.user");
            inactiveUser.setEmail("inactive@example.com");
            inactiveUser.setIsActive(false);
            
            userRepository.save(activeUser);
            userRepository.save(inactiveUser);
            entityManager.flush();
            
            // When
            List<UserJpaEntity> activeUsers = userRepository.findByIsActiveTrue();
            
            // Then
            assertThat(activeUsers).hasSize(1);
            assertThat(activeUsers.get(0).getUsername()).isEqualTo("active.user");
        }

        @Test
        @DisplayName("Should find users by role")
        void shouldFindUsersByRole() {
            // Given
            UserJpaEntity adminUser = createTestUserEntity();
            adminUser.setUsername("admin.user");
            adminUser.setEmail("admin@example.com");
            adminUser.setRole("ADMIN");
            
            UserJpaEntity regularUser = createTestUserEntity();
            regularUser.setUsername("regular.user");
            regularUser.setEmail("regular@example.com");
            regularUser.setRole("USER");
            
            userRepository.save(adminUser);
            userRepository.save(regularUser);
            entityManager.flush();
            
            // When
            List<UserJpaEntity> adminUsers = userRepository.findByRole("ADMIN");
            List<UserJpaEntity> regularUsers = userRepository.findByRole("USER");
            
            // Then
            assertThat(adminUsers).hasSize(1);
            assertThat(adminUsers.get(0).getUsername()).isEqualTo("admin.user");
            assertThat(regularUsers).hasSize(1);
            assertThat(regularUsers.get(0).getUsername()).isEqualTo("regular.user");
        }

        @Test
        @DisplayName("Should check if username exists")
        void shouldCheckIfUsernameExists() {
            // Given
            UserJpaEntity user = createTestUserEntity();
            userRepository.save(user);
            entityManager.flush();
            
            // When & Then
            assertThat(userRepository.existsByUsername("john.doe")).isTrue();
            assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("Should check if email exists")
        void shouldCheckIfEmailExists() {
            // Given
            UserJpaEntity user = createTestUserEntity();
            userRepository.save(user);
            entityManager.flush();
            
            // When & Then
            assertThat(userRepository.existsByEmail("john.doe@example.com")).isTrue();
            assertThat(userRepository.existsByEmail("nonexistent@example.com")).isFalse();
        }

        @Test
        @DisplayName("Should count active users")
        void shouldCountActiveUsers() {
            // Given
            saveTestUsers(3, true);
            saveTestUsers(2, false);
            entityManager.flush();
            
            // When
            long activeCount = userRepository.countByIsActiveTrue();
            long totalCount = userRepository.count();
            
            // Then
            assertThat(activeCount).isEqualTo(3);
            assertThat(totalCount).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Custom Queries")
    class CustomQueries {

        @Test
        @DisplayName("Should find users by name pattern")
        void shouldFindUsersByNamePattern() {
            // Given
            UserJpaEntity johnDoe = createTestUserEntity();
            johnDoe.setFirstName("John");
            johnDoe.setLastName("Doe");
            johnDoe.setUsername("john.doe");
            johnDoe.setEmail("john.doe@example.com");
            
            UserJpaEntity janeSmith = createTestUserEntity();
            janeSmith.setFirstName("Jane");
            janeSmith.setLastName("Smith");
            janeSmith.setUsername("jane.smith");
            janeSmith.setEmail("jane.smith@example.com");
            
            userRepository.save(johnDoe);
            userRepository.save(janeSmith);
            entityManager.flush();
            
            // When
            List<UserJpaEntity> johnResults = userRepository.findByNamePattern("john");
            List<UserJpaEntity> smithResults = userRepository.findByNamePattern("smith");
            
            // Then
            assertThat(johnResults).hasSize(1);
            assertThat(johnResults.get(0).getFirstName()).isEqualTo("John");
            assertThat(smithResults).hasSize(1);
            assertThat(smithResults.get(0).getLastName()).isEqualTo("Smith");
        }

        @Test
        @DisplayName("Should find users by role ignore case")
        void shouldFindUsersByRoleIgnoreCase() {
            // Given
            UserJpaEntity adminUser = createTestUserEntity();
            adminUser.setRole("ADMIN");
            adminUser.setUsername("admin.user");
            adminUser.setEmail("admin@example.com");
            
            userRepository.save(adminUser);
            entityManager.flush();
            
            // When
            List<UserJpaEntity> adminUsers = userRepository.findByRoleIgnoreCase("admin");
            
            // Then
            assertThat(adminUsers).hasSize(1);
            assertThat(adminUsers.get(0).getRole()).isEqualTo("ADMIN");
        }
    }

    @Nested
    @DisplayName("Pagination")
    class Pagination {

        @Test
        @DisplayName("Should support pagination")
        void shouldSupportPagination() {
            // Given
            saveTestUsers(10, true);
            entityManager.flush();
            
            // When
            var firstPage = userRepository.findAll(PageRequest.of(0, 3));
            var secondPage = userRepository.findAll(PageRequest.of(1, 3));
            
            // Then
            assertThat(firstPage.getContent()).hasSize(3);
            assertThat(secondPage.getContent()).hasSize(3);
            assertThat(firstPage.getTotalElements()).isEqualTo(10);
            assertThat(firstPage.getTotalPages()).isEqualTo(4);
        }
    }

    private UserJpaEntity createTestUserEntity() {
        UserJpaEntity user = new UserJpaEntity(
                UUID.randomUUID(),
                "john.doe",
                "john.doe@example.com",
                "hashed_password",
                "John",
                "Doe",
                "USER",
                true,
                null,
                null
        );
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    private void saveTestUsers(int count, boolean active) {
        for (int i = 0; i < count; i++) {
            UserJpaEntity user = new UserJpaEntity(
                    UUID.randomUUID(),
                    "user" + i,
                    "user" + i + "@example.com",
                    "hashed_password",
                    "User",
                    "Number" + i,
                    "USER",
                    active,
                    null,
                    null
            );
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }
    }
}