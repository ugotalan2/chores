package com.chores.api.repository;

import com.chores.api.entity.Chore;
import com.chores.api.enums.AssignmentType;
import com.chores.api.enums.ChoreBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChoreRepository extends JpaRepository<Chore, UUID> {

    // Active chores by block
    List<Chore> findByChoreBlockAndIsActiveTrue(ChoreBlock choreBlock);

    // Active chores by assignment type
    List<Chore> findByAssignmentTypeAndIsActiveTrue(AssignmentType assignmentType);

    // Find all active chores
    List<Chore> findByIsActiveTrue();
}
