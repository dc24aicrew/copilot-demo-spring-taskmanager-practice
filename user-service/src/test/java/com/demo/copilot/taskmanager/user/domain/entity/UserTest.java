package com.demo.copilot.taskmanager.user.domain.entity;

import com.demo.copilot.taskmanager.user.domain.valueobject.Email;
import com.demo.copilot.taskmanager.user.domain.valueobject.UserId;
import com.demo.copilot.taskmanager.user.domain.valueobject.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for User domain entity.
 * 
 * These tests validate the business logic and domain rules
 * without any infrastructure dependencies.
 */
@DisplayName("User Domain Entity")
class UserTest {

    @Nested
    @DisplayName("User Creation")
    class UserCreation {

        @Test
        @DisplayName("Should create user with valid data")
        void shouldCreateUserWithValidData() {
            // Given
            UserId id = UserId.generate();
            Email email = Email.of("john.doe@example.com");
            
            // When
            User user = new User.Builder()
                    .id(id)
                    .username("john.doe")
                    .email(email)
                    .passwordHash("hashed_password")
                    .firstName("John")
                    .lastName("Doe")
                    .role(UserRole.USER)
                    .build();
            
            // Then
            assertThat(user.getId()).isEqualTo(id);
            assertThat(user.getUsername()).isEqualTo("john.doe");
            assertThat(user.getEmail()).isEqualTo(email);
            assertThat(user.getFirstName()).isEqualTo("John");
            assertThat(user.getLastName()).isEqualTo("Doe");
            assertThat(user.getRole()).isEqualTo(UserRole.USER);
            assertThat(user.isActive()).isTrue();
            assertThat(user.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should fail when required fields are null")
        void shouldFailWhenRequiredFieldsAreNull() {
            // When & Then
            assertThatThrownBy(() -> new User.Builder()
                    .username("john.doe")
                    .email(Email.of("john@example.com"))
                    .passwordHash("hashed_password")
                    .firstName("John")
                    .lastName("Doe")
                    .build())
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("User ID cannot be null");
        }
    }

    @Nested
    @DisplayName("User Activation")
    class UserActivation {

        @Test
        @DisplayName("Should activate inactive user")
        void shouldActivateInactiveUser() {
            // Given
            User user = createTestUser();
            user.deactivate(); // Make inactive first
            
            // When
            user.activate();
            
            // Then
            assertThat(user.isActive()).isTrue();
            assertThat(user.getUpdatedAt()).isAfter(user.getCreatedAt());
        }

        @Test
        @DisplayName("Should fail to activate already active user")
        void shouldFailToActivateAlreadyActiveUser() {
            // Given
            User user = createTestUser(); // Active by default
            
            // When & Then
            assertThatThrownBy(user::activate)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("User is already active");
        }
    }

    @Nested
    @DisplayName("User Deactivation")
    class UserDeactivation {

        @Test
        @DisplayName("Should deactivate active user")
        void shouldDeactivateActiveUser() {
            // Given
            User user = createTestUser(); // Active by default
            
            // When
            user.deactivate();
            
            // Then
            assertThat(user.isActive()).isFalse();
        }

        @Test
        @DisplayName("Should fail to deactivate already inactive user")
        void shouldFailToDeactivateAlreadyInactiveUser() {
            // Given
            User user = createTestUser();
            user.deactivate(); // Make inactive first
            
            // When & Then
            assertThatThrownBy(user::deactivate)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("User is already inactive");
        }
    }

    @Nested
    @DisplayName("Login Recording")
    class LoginRecording {

        @Test
        @DisplayName("Should record login for active user")
        void shouldRecordLoginForActiveUser() {
            // Given
            User user = createTestUser(); // Active by default
            
            // When
            user.recordLogin();
            
            // Then
            assertThat(user.getLastLoginAt()).isNotNull();
            assertThat(user.getLastLoginAt()).isBeforeOrEqualTo(LocalDateTime.now());
        }

        @Test
        @DisplayName("Should fail to record login for inactive user")
        void shouldFailToRecordLoginForInactiveUser() {
            // Given
            User user = createTestUser();
            user.deactivate(); // Make inactive
            
            // When & Then
            assertThatThrownBy(user::recordLogin)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot record login for inactive user");
        }
    }

    @Nested
    @DisplayName("Password Management")
    class PasswordManagement {

        @Test
        @DisplayName("Should change password with valid hash")
        void shouldChangePasswordWithValidHash() {
            // Given
            User user = createTestUser();
            String newPasswordHash = "new_hashed_password";
            
            // When
            user.changePassword(newPasswordHash);
            
            // Then
            assertThat(user.getPasswordHash()).isEqualTo(newPasswordHash);
        }

        @Test
        @DisplayName("Should fail to change password with null hash")
        void shouldFailToChangePasswordWithNullHash() {
            // Given
            User user = createTestUser();
            
            // When & Then
            assertThatThrownBy(() -> user.changePassword(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Password hash cannot be null or empty");
        }

        @Test
        @DisplayName("Should fail to change password with empty hash")
        void shouldFailToChangePasswordWithEmptyHash() {
            // Given
            User user = createTestUser();
            
            // When & Then
            assertThatThrownBy(() -> user.changePassword("  "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Password hash cannot be null or empty");
        }
    }

    @Nested
    @DisplayName("Profile Updates")
    class ProfileUpdates {

        @Test
        @DisplayName("Should update profile with valid data")
        void shouldUpdateProfileWithValidData() {
            // Given
            User user = createTestUser();
            
            // When
            user.updateProfile("Jane", "Smith", "https://example.com/avatar.jpg");
            
            // Then
            assertThat(user.getFirstName()).isEqualTo("Jane");
            assertThat(user.getLastName()).isEqualTo("Smith");
            assertThat(user.getAvatarUrl()).isEqualTo("https://example.com/avatar.jpg");
        }

        @Test
        @DisplayName("Should not update profile with null values")
        void shouldNotUpdateProfileWithNullValues() {
            // Given
            User user = createTestUser();
            String originalFirstName = user.getFirstName();
            String originalLastName = user.getLastName();
            
            // When
            user.updateProfile(null, null, null);
            
            // Then
            assertThat(user.getFirstName()).isEqualTo(originalFirstName);
            assertThat(user.getLastName()).isEqualTo(originalLastName);
            assertThat(user.getAvatarUrl()).isNull();
        }
    }

    @Nested
    @DisplayName("Role Management")
    class RoleManagement {

        @Test
        @DisplayName("Should identify admin user")
        void shouldIdentifyAdminUser() {
            // Given
            User user = createTestUser();
            user.changeRole(UserRole.ADMIN);
            
            // When & Then
            assertThat(user.isAdmin()).isTrue();
            assertThat(user.isManager()).isFalse();
            assertThat(user.canManageUsers()).isTrue();
        }

        @Test
        @DisplayName("Should identify manager user")
        void shouldIdentifyManagerUser() {
            // Given
            User user = createTestUser();
            user.changeRole(UserRole.MANAGER);
            
            // When & Then
            assertThat(user.isManager()).isTrue();
            assertThat(user.isAdmin()).isFalse();
            assertThat(user.canManageUsers()).isTrue();
        }

        @Test
        @DisplayName("Should identify regular user")
        void shouldIdentifyRegularUser() {
            // Given
            User user = createTestUser(); // USER role by default
            
            // When & Then
            assertThat(user.isAdmin()).isFalse();
            assertThat(user.isManager()).isFalse();
            assertThat(user.canManageUsers()).isFalse();
        }
    }

    @Nested
    @DisplayName("Business Logic")
    class BusinessLogic {

        @Test
        @DisplayName("Should return full name")
        void shouldReturnFullName() {
            // Given
            User user = createTestUser();
            
            // When
            String fullName = user.getFullName();
            
            // Then
            assertThat(fullName).isEqualTo("John Doe");
        }

        @Test
        @DisplayName("Should check recent login correctly")
        void shouldCheckRecentLoginCorrectly() {
            // Given
            User user = createTestUser();
            
            // When & Then (no login recorded)
            assertThat(user.hasLoggedInRecently(7)).isFalse();
            
            // When (record login)
            user.recordLogin();
            
            // Then
            assertThat(user.hasLoggedInRecently(7)).isTrue();
        }
    }

    private User createTestUser() {
        return new User.Builder()
                .id(UserId.generate())
                .username("john.doe")
                .email(Email.of("john.doe@example.com"))
                .passwordHash("hashed_password")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.USER)
                .build();
    }
}