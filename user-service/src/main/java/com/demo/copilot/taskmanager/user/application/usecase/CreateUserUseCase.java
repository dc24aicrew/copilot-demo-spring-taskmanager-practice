package com.demo.copilot.taskmanager.user.application.usecase;

import com.demo.copilot.taskmanager.user.domain.entity.User;
import com.demo.copilot.taskmanager.user.domain.repository.UserRepository;
import com.demo.copilot.taskmanager.user.domain.service.UserDomainService;
import com.demo.copilot.taskmanager.user.domain.valueobject.Email;
import com.demo.copilot.taskmanager.user.domain.valueobject.UserId;
import com.demo.copilot.taskmanager.user.domain.valueobject.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for creating new users.
 * 
 * This use case orchestrates the process of creating a new user,
 * including validation, domain logic, and persistence.
 */
@Service
@Transactional
public class CreateUserUseCase {

    private final UserRepository userRepository;
    private final UserDomainService userDomainService;

    public CreateUserUseCase(UserRepository userRepository, UserDomainService userDomainService) {
        this.userRepository = userRepository;
        this.userDomainService = userDomainService;
    }

    /**
     * Creates a new user with the provided details.
     * 
     * @param command the command containing user creation details
     * @return the created user
     * @throws IllegalArgumentException if validation fails
     */
    public User execute(CreateUserCommand command) {
        // Validate input
        validateCommand(command);

        // Create domain value objects
        Email email = Email.of(command.email());
        UserId userId = UserId.generate();

        // Domain validation
        userDomainService.validateUniqueUsername(command.username());
        userDomainService.validateUniqueEmail(email);
        userDomainService.validatePasswordStrength(command.password());

        // Hash password (in a real application, use proper password hashing)
        String passwordHash = hashPassword(command.password());

        // Create domain entity
        User user = new User.Builder()
                .id(userId)
                .username(command.username())
                .email(email)
                .passwordHash(passwordHash)
                .firstName(command.firstName())
                .lastName(command.lastName())
                .role(command.role())
                .isActive(true)
                .avatarUrl(command.avatarUrl())
                .build();

        // Persist the user
        return userRepository.save(user);
    }

    private void validateCommand(CreateUserCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Create user command cannot be null");
        }
        if (command.username() == null || command.username().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (command.email() == null || command.email().trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (command.password() == null || command.password().trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        if (command.firstName() == null || command.firstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be null or empty");
        }
        if (command.lastName() == null || command.lastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be null or empty");
        }
    }

    private String hashPassword(String password) {
        // TODO: Implement proper password hashing using BCrypt or similar
        // For now, just return a placeholder
        return "hashed_" + password;
    }

    /**
     * Command for creating a user.
     */
    public record CreateUserCommand(
            String username,
            String email,
            String password,
            String firstName,
            String lastName,
            UserRole role,
            String avatarUrl
    ) {
        public CreateUserCommand {
            // Default role if not provided
            if (role == null) {
                role = UserRole.USER;
            }
        }
    }
}