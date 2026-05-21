package com.bitly.repository;

import com.bitly.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA repository for User entity CRUD and lookup operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their username.
     */
    Optional<User> findByUsername(String username);

    /**
     * Checks if a username already exists.
     */
    boolean existsByUsername(String username);

    /**
     * Checks if an email already exists.
     */
    boolean existsByEmail(String email);
}
