package com.demo.copilot.taskmanager.user.domain.entity;

import com.demo.copilot.taskmanager.user.domain.valueobject.Email;
import com.demo.copilot.taskmanager.user.domain.valueobject.UserId;
import com.demo.copilot.taskmanager.user.domain.valueobject.UserRole;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Pure domain entity representing a system user.
 * 
 * This entity follows Domain-Driven Design principles and encapsulates
 * business logic related to user management.
 * 
 * NOTE: This is a PURE domain entity with NO infrastructure concerns.
 * JPA annotations and persistence logic are handled in the infrastructure layer.
 */
public class User {

    private final UserId id;
    private String username;
    private Email email;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private UserRole role;
    private boolean isActive;
    private LocalDateTime lastLoginAt;
    private String avatarUrl;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private User(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "User ID cannot be null");
        this.username = Objects.requireNonNull(builder.username, "Username cannot be null");
        this.email = Objects.requireNonNull(builder.email, "Email cannot be null");
        this.passwordHash = Objects.requireNonNull(builder.passwordHash, "Password hash cannot be null");
        this.firstName = Objects.requireNonNull(builder.firstName, "First name cannot be null");
        this.lastName = Objects.requireNonNull(builder.lastName, "Last name cannot be null");
        this.role = builder.role != null ? builder.role : UserRole.USER;
        this.isActive = builder.isActive != null ? builder.isActive : true;
        this.avatarUrl = builder.avatarUrl;
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Business methods - the core of domain logic
    
    /**
     * Activates the user account.
     * Business rule: Only inactive users can be activated.
     */
    public void activate() {
        if (this.isActive) {
            throw new IllegalStateException("User is already active");
        }
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Deactivates the user account.
     * Business rule: Active users can be deactivated for security reasons.
     */
    public void deactivate() {
        if (!this.isActive) {
            throw new IllegalStateException("User is already inactive");
        }
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Records a successful login attempt.
     * Business rule: Only active users can login.
     */
    public void recordLogin() {
        if (!this.isActive) {
            throw new IllegalStateException("Cannot record login for inactive user");
        }
        this.lastLoginAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Changes the user's password.
     * Business rule: Password hash cannot be null or empty.
     */
    public void changePassword(String newPasswordHash) {
        if (newPasswordHash == null || newPasswordHash.trim().isEmpty()) {
            throw new IllegalArgumentException("Password hash cannot be null or empty");
        }
        this.passwordHash = newPasswordHash;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates the user's profile information.
     * Business rule: Only non-null values are updated.
     */
    public void updateProfile(String firstName, String lastName, String avatarUrl) {
        boolean changed = false;
        
        if (firstName != null && !firstName.trim().isEmpty()) {
            this.firstName = firstName.trim();
            changed = true;
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            this.lastName = lastName.trim();
            changed = true;
        }
        if (avatarUrl != null) {
            this.avatarUrl = avatarUrl.trim().isEmpty() ? null : avatarUrl.trim();
            changed = true;
        }
        
        if (changed) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Updates the user's email address.
     * Business rule: Email must be valid and unique (uniqueness checked by repository).
     */
    public void updateEmail(Email newEmail) {
        if (newEmail == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }
        if (!this.email.equals(newEmail)) {
            this.email = newEmail;
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Promotes or demotes the user's role.
     * Business rule: Role changes should be logged for audit purposes.
     */
    public void changeRole(UserRole newRole) {
        if (newRole == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        if (!this.role.equals(newRole)) {
            this.role = newRole;
            this.updatedAt = LocalDateTime.now();
        }
    }

    // Query methods for business logic
    
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isAdmin() {
        return UserRole.ADMIN.equals(this.role);
    }

    public boolean isManager() {
        return UserRole.MANAGER.equals(this.role);
    }

    public boolean hasLoggedInRecently(int daysThreshold) {
        if (lastLoginAt == null) {
            return false;
        }
        return lastLoginAt.isAfter(LocalDateTime.now().minusDays(daysThreshold));
    }

    public boolean canManageUsers() {
        return isAdmin() || isManager();
    }

    // Getters (immutable access)
    public UserId getId() { return id; }
    public String getUsername() { return username; }
    public Email getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public UserRole getRole() { return role; }
    public boolean isActive() { return isActive; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public String getAvatarUrl() { return avatarUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{" +
               "id=" + id +
               ", username='" + username + '\'' +
               ", email=" + email +
               ", firstName='" + firstName + '\'' +
               ", lastName='" + lastName + '\'' +
               ", role=" + role +
               ", isActive=" + isActive +
               '}';
    }

    // Builder pattern for construction
    public static class Builder {
        private UserId id;
        private String username;
        private Email email;
        private String passwordHash;
        private String firstName;
        private String lastName;
        private UserRole role = UserRole.USER;
        private Boolean isActive = true;
        private String avatarUrl;
        private LocalDateTime createdAt;

        public Builder id(UserId id) {
            this.id = id;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder email(Email email) {
            this.email = email;
            return this;
        }

        public Builder passwordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder role(UserRole role) {
            this.role = role;
            return this;
        }

        public Builder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public Builder avatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}