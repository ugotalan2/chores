package com.chores.api.repository;

import com.chores.api.entity.User;
import com.chores.api.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Find all active kids or parents
    List<User> findByRoleAndIsActiveTrue(Role role);

    // Find parent by Clerk OAuth Id (used on login)
    Optional<User> findByClerkUserId(String clerkUserId);

    // Find parent by email
    Optional<User> findByEmail(String email);
}
