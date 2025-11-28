package com.example.financemanager.services;

import com.example.financemanager.entities.User;
import com.example.financemanager.exceptions.UserAlreadyExistsException;
import com.example.financemanager.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service class for user management and authentication.
 * Implements Spring Security's UserDetailsService for authentication integration.
 *
 * <p>This service handles:</p>
 * <ul>
 *   <li>User registration with validation and password encryption</li>
 *   <li>User authentication through Spring Security</li>
 *   <li>Uniqueness validation for usernames and emails</li>
 * </ul>
 *
 * <p>All passwords are encrypted using BCrypt before storage.
 * No plain text passwords are ever persisted to the database.</p>
 *
 * @author Finance Manager Team
 * @version 1.0
 * @since 2025-01-28
 * @see User
 * @see UserDetailsService
 */
@Service
public class UserService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    /**
     * Creates a new user account with encrypted password.
     * Validates that username and email are unique before creating the account.
     *
     * @param username the desired username (must be unique, 3-50 characters)
     * @param email the user's email address (must be unique and valid format)
     * @param password the plain text password (will be encrypted with BCrypt)
     * @throws UserAlreadyExistsException if the username or email is already taken
     */
    public void createUser(String username,  String email, String password) {
        // Check if username already exists
        if (userRepository.findByUsername(username) != null) {
            throw UserAlreadyExistsException.forUsername(username);
        }

        // Check if email already exists
        User existingEmailUser = userRepository.findByEmail(email);
        if (existingEmailUser != null) {
            throw UserAlreadyExistsException.forEmail(email);
        }

        // Encrypt the password before saving
        String encryptedPassword = passwordEncoder.encode(password);
        User newUser = new User(username, email, encryptedPassword);
        userRepository.save(newUser);
    }

    /**
     * Loads user details by username for Spring Security authentication.
     * Required implementation of UserDetailsService interface.
     *
     * <p>This method is called by Spring Security during the authentication process
     * to retrieve user credentials and authorities.</p>
     *
     * @param username the username to search for
     * @return UserDetails object containing user authentication information
     * @throws UsernameNotFoundException if no user found with the given username
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        // Convert application User entity to Spring Security's UserDetails
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())  // Already BCrypt encrypted
                .authorities("USER")  // All users have USER role
                .build();
    }
}
