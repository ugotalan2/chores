package com.chores.api.repository;

import com.chores.api.entity.Assignment;
import com.chores.api.entity.User;
import com.chores.api.enums.AssignmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, UUID> {

    // Find by User + assignment type + is active
    List<Assignment> findByUserAndAssignmentTypeAndIsActiveTrue(User user, AssignmentType assignmentType);

    // All active assignments for a user
    List<Assignment> findByUserAndIsActiveTrue(User user);

    // All active assignments by type
    List<Assignment> findByAssignmentTypeAndIsActiveTrue(AssignmentType assignmentType);
}
