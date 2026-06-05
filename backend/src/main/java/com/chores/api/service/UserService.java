package com.chores.api.service;

import com.chores.api.entity.User;
import com.chores.api.enums.Role;
import com.chores.api.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Get all active kids
    public List<User> getActiveKids() {
        return userRepository.findByRoleAndIsActiveTrue(Role.CHILD);
    }

    // Get all active parents
    public List<User> getActiveParents() {
        return userRepository.findByRoleAndIsActiveTrue(Role.PARENT);
    }

    // Get user by ID
    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    // Get parent by Clerk user ID (used on login)
    public Optional<User> getUserByClerkId(String clerkUserId) {
        return userRepository.findByClerkUserId(clerkUserId);
    }

    // Create a new user
    @Transactional
    public User createUser(User user) {
        return userRepository.save(user);
    }

    // Update an existing user
    @Transactional
    public User updateUser(UUID id, User user) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException(
                "User not found: " + id);
        }
        // make sure the ids match
        if (user.getId() != null && !user.getId().equals(id)) {
            throw new IllegalArgumentException(
                "Path ID and body ID do not match");
        }
        user.setId(id); // force the ID as it could have been null
        return userRepository.save(user);
    }

    // Soft-delete - sets isActive to false
    @Transactional
    public void deactivateUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User not found: " + id));
        user.setIsActive(false);
        userRepository.save(user);
    }
}
