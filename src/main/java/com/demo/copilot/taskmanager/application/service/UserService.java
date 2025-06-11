package com.demo.copilot.taskmanager.application.service;

import com.demo.copilot.taskmanager.application.dto.user.CreateUserRequest;
import com.demo.copilot.taskmanager.application.dto.user.UpdateUserRequest;
import com.demo.copilot.taskmanager.application.dto.user.UserResponse;
import com.demo.copilot.taskmanager.application.exception.DuplicateEmailException;
import com.demo.copilot.taskmanager.application.exception.DuplicateUsernameException;
import com.demo.copilot.taskmanager.application.exception.UserNotFoundException;
import com.demo.copilot.taskmanager.application.mapper.UserMapper;
import com.demo.copilot.taskmanager.domain.entity.User;
import com.demo.copilot.taskmanager.domain.valueobject.Email;
import com.demo.copilot.taskmanager.domain.valueobject.UserId;
import com.demo.copilot.taskmanager.infrastructure.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Enhanced application service for user management operations with caching.
 * 
 * This service orchestrates user-related business operations and
 * coordinates between the domain layer and infrastructure layer.
 * 
 * Caching strategy:
 * - Users are cached for 30 minutes
 * - Cache is invalidated on user updates and deletions
 * - Frequently accessed users benefit from reduced database queries
 */
@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, 
                      UserMapper userMapper, 
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Create a new user.
     */
    public UserResponse createUser(CreateUserRequest request) {
        // Check for duplicate email
        Email email = Email.of(request.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("User with email " + request.getEmail() + " already exists");
        }

        // Check for duplicate username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUsernameException("User with username " + request.getUsername() + " already exists");
        }

        // Hash the password
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // Create user entity
        User user = new User.Builder()
                .id(UserId.generate())
                .username(request.getUsername())
                .email(email)
                .passwordHash(hashedPassword)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(request.getRole())
                .isActive(true)
                .build();

        // Save and return response
        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    /**
     * Get user by ID with caching.
     */
    @Cacheable(value = "users", key = "#id.value", unless = "#result == null")
    @Transactional(readOnly = true)
    public UserResponse getUserById(UserId id) {
        logger.debug("Fetching user by ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        return userMapper.toResponse(user);
    }

    /**
     * Get user by email with caching.
     */
    @Cacheable(value = "users", key = "'email:' + #emailAddress", unless = "#result == null")
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String emailAddress) {
        logger.debug("Fetching user by email: {}", emailAddress);
        Email email = Email.of(emailAddress);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + emailAddress));
        return userMapper.toResponse(user);
    }

    /**
     * Get user by username with caching.
     */
    @Cacheable(value = "users", key = "'username:' + #username", unless = "#result == null")
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        logger.debug("Fetching user by username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        return userMapper.toResponse(user);
    }

    /**
     * Get all users.
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all active users with caching.
     */
    @Cacheable(value = "users", key = "'active-users'")
    @Transactional(readOnly = true)
    public List<UserResponse> getActiveUsers() {
        logger.debug("Fetching all active users");
        return userRepository.findByIsActiveTrue()
                .stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update user information with cache eviction.
     */
    @Caching(evict = {
        @CacheEvict(value = "users", key = "#id.value"),
        @CacheEvict(value = "users", key = "'email:' + #result.email"),
        @CacheEvict(value = "users", key = "'username:' + #result.username"),
        @CacheEvict(value = "users", key = "'active-users'", condition = "#result.isActive")
    })
    public UserResponse updateUser(UserId id, UpdateUserRequest request) {
        logger.debug("Updating user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        // Update email if provided and different
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail().getValue())) {
            Email newEmail = Email.of(request.getEmail());
            if (userRepository.existsByEmail(newEmail)) {
                throw new DuplicateEmailException("User with email " + request.getEmail() + " already exists");
            }
            // Note: Email update would require updating the embedded Email value object
        }

        // Update username if provided and different
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new DuplicateUsernameException("User with username " + request.getUsername() + " already exists");
            }
            // Note: Username update would require reflection or setter method
        }

        // Update profile information
        user.updateProfile(request.getFirstName(), request.getLastName(), request.getAvatarUrl());

        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    /**
     * Activate a user with cache eviction.
     */
    @Caching(evict = {
        @CacheEvict(value = "users", key = "#id.value"),
        @CacheEvict(value = "users", key = "'active-users'")
    })
    public UserResponse activateUser(UserId id) {
        logger.debug("Activating user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        
        user.activate();
        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    /**
     * Deactivate a user with cache eviction.
     */
    @Caching(evict = {
        @CacheEvict(value = "users", key = "#id.value"),
        @CacheEvict(value = "users", key = "'active-users'")
    })
    public UserResponse deactivateUser(UserId id) {
        logger.debug("Deactivating user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        
        user.deactivate();
        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    /**
     * Delete a user with cache eviction.
     */
    @Caching(evict = {
        @CacheEvict(value = "users", key = "#id.value"),
        @CacheEvict(value = "users", key = "'active-users'"),
        @CacheEvict(value = "users", allEntries = true) // Clear all user cache entries
    })
    public void deleteUser(UserId id) {
        logger.debug("Deleting user with ID: {}", id);
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
    }

    /**
     * Change user password with cache eviction.
     */
    @CacheEvict(value = "users", key = "#id.value")
    public void changePassword(UserId id, String newPassword) {
        logger.debug("Changing password for user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        
        String hashedPassword = passwordEncoder.encode(newPassword);
        user.changePassword(hashedPassword);
        userRepository.save(user);
    }

    /**
     * Update last login timestamp.
     */
    public void updateLastLogin(UserId id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        
        user.updateLastLogin();
        userRepository.save(user);
    }
}
