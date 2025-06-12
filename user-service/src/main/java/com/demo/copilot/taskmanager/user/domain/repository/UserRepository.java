package com.demo.copilot.taskmanager.user.domain.repository;

import com.demo.copilot.taskmanager.user.domain.entity.User;
import com.demo.copilot.taskmanager.user.domain.valueobject.Email;
import com.demo.copilot.taskmanager.user.domain.valueobject.UserId;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository contract for User aggregate.
 * 
 * This interface defines the contract for persisting and retrieving User entities
 * without any infrastructure dependencies. It's part of the domain layer and
 * follows the Repository pattern from Domain-Driven Design.
 * 
 * Implementations of this interface should be provided in the infrastructure layer.
 */
public interface UserRepository {

    /**
     * Saves a user entity.
     * 
     * @param user the user to save
     * @return the saved user with any generated values
     */
    User save(User user);

    /**
     * Finds a user by their unique identifier.
     * 
     * @param id the user identifier
     * @return the user if found, empty otherwise
     */
    Optional<User> findById(UserId id);

    /**
     * Finds a user by their username.
     * 
     * @param username the username to search for
     * @return the user if found, empty otherwise
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by their email address.
     * 
     * @param email the email to search for
     * @return the user if found, empty otherwise
     */
    Optional<User> findByEmail(Email email);

    /**
     * Finds all active users.
     * 
     * @return list of active users
     */
    List<User> findAllActive();

    /**
     * Finds all users with pagination support.
     * 
     * @param offset the number of items to skip
     * @param limit the maximum number of items to return
     * @return list of users
     */
    List<User> findAll(int offset, int limit);

    /**
     * Counts the total number of users.
     * 
     * @return total count of users
     */
    long count();

    /**
     * Counts the number of active users.
     * 
     * @return count of active users
     */
    long countActive();

    /**
     * Checks if a username already exists.
     * 
     * @param username the username to check
     * @return true if username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Checks if an email address already exists.
     * 
     * @param email the email to check
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(Email email);

    /**
     * Deletes a user by their identifier.
     * 
     * @param id the user identifier
     */
    void deleteById(UserId id);

    /**
     * Finds users by their role.
     * 
     * @param role the role to search for
     * @return list of users with the specified role
     */
    List<User> findByRole(String role);
}