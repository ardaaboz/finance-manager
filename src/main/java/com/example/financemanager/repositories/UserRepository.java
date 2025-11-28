package com.example.financemanager.repositories;

import com.example.financemanager.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for User entity data access.
 * Extends JpaRepository to provide CRUD operations and custom query methods.
 *
 * @author Finance Manager Team
 * @version 1.0
 * @since 2025-01-28
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Finds a user by their username.
     *
     * @param username the username to search for
     * @return the User entity or null if not found
     */
    User findByUsername(String username);

    /**
     * Finds a user by their email address.
     *
     * @param email the email to search for
     * @return the User entity or null if not found
     */
    User findByEmail(String email);
}