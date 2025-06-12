package com.demo.copilot.taskmanager.user.infrastructure.persistence.repository;

import com.demo.copilot.taskmanager.user.domain.entity.User;
import com.demo.copilot.taskmanager.user.domain.repository.UserRepository;
import com.demo.copilot.taskmanager.user.domain.valueobject.Email;
import com.demo.copilot.taskmanager.user.domain.valueobject.UserId;
import com.demo.copilot.taskmanager.user.infrastructure.persistence.entity.UserJpaEntity;
import com.demo.copilot.taskmanager.user.infrastructure.persistence.mapper.UserEntityMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the domain UserRepository using JPA.
 * 
 * This adapter implements the domain repository contract using
 * Spring Data JPA, providing a bridge between the domain layer
 * and the persistence infrastructure.
 */
@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final UserEntityMapper mapper;

    public UserRepositoryImpl(UserJpaRepository jpaRepository, UserEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        UserJpaEntity jpaEntity;
        
        // Check if this is an update of an existing entity
        Optional<UserJpaEntity> existingEntity = jpaRepository.findById(user.getId().getValue());
        
        if (existingEntity.isPresent()) {
            // Update existing entity to preserve JPA-managed fields
            jpaEntity = existingEntity.get();
            mapper.updateJpaEntity(jpaEntity, user);
        } else {
            // Create new entity
            jpaEntity = mapper.toJpaEntity(user);
        }
        
        UserJpaEntity savedEntity = jpaRepository.save(jpaEntity);
        return mapper.toDomainEntity(savedEntity);
    }

    @Override
    public Optional<User> findById(UserId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomainEntity);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username)
                .map(mapper::toDomainEntity);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return jpaRepository.findByEmail(email.getValue())
                .map(mapper::toDomainEntity);
    }

    @Override
    public List<User> findAllActive() {
        return jpaRepository.findByIsActiveTrue()
                .stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findAll(int offset, int limit) {
        PageRequest pageRequest = PageRequest.of(offset / limit, limit);
        return jpaRepository.findAll(pageRequest)
                .getContent()
                .stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public long countActive() {
        return jpaRepository.countByIsActiveTrue();
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpaRepository.existsByEmail(email.getValue());
    }

    @Override
    public void deleteById(UserId id) {
        jpaRepository.deleteById(id.getValue());
    }

    @Override
    public List<User> findByRole(String role) {
        return jpaRepository.findByRole(role)
                .stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }
}