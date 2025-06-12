package com.demo.copilot.taskmanager.user.infrastructure.persistence.mapper;

import com.demo.copilot.taskmanager.user.domain.entity.User;
import com.demo.copilot.taskmanager.user.domain.valueobject.Email;
import com.demo.copilot.taskmanager.user.domain.valueobject.UserId;
import com.demo.copilot.taskmanager.user.domain.valueobject.UserRole;
import com.demo.copilot.taskmanager.user.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between domain User entity and JPA UserEntity.
 * 
 * This mapper provides a clean separation between the domain model
 * and the persistence model, allowing each layer to evolve independently.
 */
@Component
public class UserEntityMapper {

    /**
     * Converts a domain User entity to a JPA entity for persistence.
     * 
     * @param domainUser the domain user entity
     * @return the JPA entity representation
     */
    public UserJpaEntity toJpaEntity(User domainUser) {
        if (domainUser == null) {
            return null;
        }

        UserJpaEntity jpaEntity = new UserJpaEntity(
                domainUser.getId().getValue(),
                domainUser.getUsername(),
                domainUser.getEmail().getValue(),
                domainUser.getPasswordHash(),
                domainUser.getFirstName(),
                domainUser.getLastName(),
                domainUser.getRole().name(),
                domainUser.isActive(),
                domainUser.getLastLoginAt(),
                domainUser.getAvatarUrl()
        );

        // Set timestamps if available
        if (domainUser.getCreatedAt() != null) {
            jpaEntity.setCreatedAt(domainUser.getCreatedAt());
        }
        if (domainUser.getUpdatedAt() != null) {
            jpaEntity.setUpdatedAt(domainUser.getUpdatedAt());
        }

        return jpaEntity;
    }

    /**
     * Converts a JPA entity to a domain User entity.
     * 
     * @param jpaEntity the JPA entity from persistence
     * @return the domain user entity
     */
    public User toDomainEntity(UserJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }

        return new User.Builder()
                .id(UserId.of(jpaEntity.getId()))
                .username(jpaEntity.getUsername())
                .email(Email.of(jpaEntity.getEmail()))
                .passwordHash(jpaEntity.getPasswordHash())
                .firstName(jpaEntity.getFirstName())
                .lastName(jpaEntity.getLastName())
                .role(UserRole.valueOf(jpaEntity.getRole()))
                .isActive(jpaEntity.getIsActive())
                .avatarUrl(jpaEntity.getAvatarUrl())
                .createdAt(jpaEntity.getCreatedAt())
                .build();
    }

    /**
     * Updates a JPA entity with values from a domain entity.
     * 
     * This method is useful for updating existing entities while preserving
     * JPA-managed fields like version and creation timestamps.
     * 
     * @param jpaEntity the JPA entity to update
     * @param domainUser the domain entity with new values
     */
    public void updateJpaEntity(UserJpaEntity jpaEntity, User domainUser) {
        if (jpaEntity == null || domainUser == null) {
            return;
        }

        jpaEntity.setUsername(domainUser.getUsername());
        jpaEntity.setEmail(domainUser.getEmail().getValue());
        jpaEntity.setPasswordHash(domainUser.getPasswordHash());
        jpaEntity.setFirstName(domainUser.getFirstName());
        jpaEntity.setLastName(domainUser.getLastName());
        jpaEntity.setRole(domainUser.getRole().name());
        jpaEntity.setIsActive(domainUser.isActive());
        jpaEntity.setLastLoginAt(domainUser.getLastLoginAt());
        jpaEntity.setAvatarUrl(domainUser.getAvatarUrl());
        
        // Updated timestamp will be handled by @LastModifiedDate
    }
}