package com.demo.copilot.taskmanager.user.domain.service;

import com.demo.copilot.taskmanager.user.domain.entity.User;
import com.demo.copilot.taskmanager.user.domain.valueobject.Email;
import com.demo.copilot.taskmanager.user.domain.repository.UserRepository;

/**
 * Domain service for User-related business logic.
 * 
 * This service encapsulates complex business rules that don't naturally
 * fit within a single entity or value object. It operates purely in the
 * domain layer without any infrastructure dependencies.
 */
public class UserDomainService {

    private final UserRepository userRepository;

    public UserDomainService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Validates that a username is unique in the system.
     * 
     * Business rule: Usernames must be unique across all users.
     * 
     * @param username the username to validate
     * @throws IllegalArgumentException if username already exists
     */
    public void validateUniqueUsername(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken");
        }
    }

    /**
     * Validates that an email address is unique in the system.
     * 
     * Business rule: Email addresses must be unique across all users.
     * 
     * @param email the email to validate
     * @throws IllegalArgumentException if email already exists
     */
    public void validateUniqueEmail(Email email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email '" + email.getValue() + "' is already registered");
        }
    }

    /**
     * Validates that a user can be updated with new username and email.
     * 
     * Business rule: Username and email must be unique, but user can keep their current values.
     * 
     * @param user the user being updated
     * @param newUsername the new username (can be null if not changing)
     * @param newEmail the new email (can be null if not changing)
     * @throws IllegalArgumentException if username or email conflicts with existing users
     */
    public void validateUserUpdate(User user, String newUsername, Email newEmail) {
        // Check username uniqueness if it's being changed
        if (newUsername != null && !newUsername.equals(user.getUsername())) {
            validateUniqueUsername(newUsername);
        }

        // Check email uniqueness if it's being changed
        if (newEmail != null && !newEmail.equals(user.getEmail())) {
            validateUniqueEmail(newEmail);
        }
    }

    /**
     * Determines if a user can be deleted based on business rules.
     * 
     * Business rule: Admin users cannot be deleted if they are the last admin.
     * 
     * @param user the user to check for deletion eligibility
     * @return true if user can be deleted, false otherwise
     */
    public boolean canDeleteUser(User user) {
        if (!user.isAdmin()) {
            return true; // Non-admin users can always be deleted
        }

        // Count other active admin users
        long activeAdminCount = userRepository.findByRole("ADMIN")
                .stream()
                .filter(User::isActive)
                .filter(u -> !u.getId().equals(user.getId()))
                .count();

        return activeAdminCount > 0; // Can delete if there are other active admins
    }

    /**
     * Validates password strength according to business rules.
     * 
     * Business rule: Passwords must meet minimum security requirements.
     * 
     * @param password the plain text password to validate
     * @throws IllegalArgumentException if password doesn't meet requirements
     */
    public void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }

        if (!password.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
        }

        if (!password.matches(".*[0-9].*")) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }

        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new IllegalArgumentException("Password must contain at least one special character");
        }
    }

    /**
     * Generates a suggested username based on first and last name.
     * 
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @return a suggested unique username
     */
    public String generateUniqueUsername(String firstName, String lastName) {
        String baseUsername = (firstName + "." + lastName).toLowerCase()
                .replaceAll("[^a-zA-Z0-9.]", "");

        String username = baseUsername;
        int counter = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }
}