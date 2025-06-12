package com.demo.copilot.taskmanager.user.infrastructure.persistence.repository;

import com.demo.copilot.taskmanager.user.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for UserJpaEntity.
 * 
 * This interface provides the actual database access methods
 * using Spring Data JPA conventions and custom queries.
 */
@Repository
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {

    /**
     * Finds a user by username.
     */
    Optional<UserJpaEntity> findByUsername(String username);

    /**
     * Finds a user by email address.
     */
    Optional<UserJpaEntity> findByEmail(String email);

    /**
     * Finds all active users.
     */
    List<UserJpaEntity> findByIsActiveTrue();

    /**
     * Finds users by role.
     */
    List<UserJpaEntity> findByRole(String role);

    /**
     * Finds active users by role.
     */
    List<UserJpaEntity> findByRoleAndIsActiveTrue(String role);

    /**
     * Checks if a username exists.
     */
    boolean existsByUsername(String username);

    /**
     * Checks if an email exists.
     */
    boolean existsByEmail(String email);

    /**
     * Counts active users.
     */
    long countByIsActiveTrue();

    /**
     * Finds users with pagination.
     */
    Page<UserJpaEntity> findAll(Pageable pageable);

    /**
     * Finds active users with pagination.
     */
    Page<UserJpaEntity> findByIsActiveTrue(Pageable pageable);

    /**
     * Custom query to find users by role with case-insensitive search.
     */
    @Query("SELECT u FROM UserJpaEntity u WHERE UPPER(u.role) = UPPER(:role)")
    List<UserJpaEntity> findByRoleIgnoreCase(@Param("role") String role);

    /**
     * Custom query to search users by name pattern.
     */
    @Query("SELECT u FROM UserJpaEntity u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<UserJpaEntity> findByNamePattern(@Param("searchTerm") String searchTerm);
}