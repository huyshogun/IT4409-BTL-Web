package com.example.auth.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.auth.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @Cacheable(value = "users", key = "#username")
    Optional<User> findByUserName(String username);

    @Cacheable(value = "users", key = "'exists_' + #username")
    Boolean existsByUserName(String username);

    @Cacheable(value = "users", key = "'email_' + #email")
    Boolean existsByEmail(String email);

    @Override
    @Cacheable(value = "users", key = "#id")
    Optional<User> findById(UUID id);

    @Override
    @CacheEvict(value = {"users", "userDetails"}, allEntries = true)
    <S extends User> S save(S entity);
}
