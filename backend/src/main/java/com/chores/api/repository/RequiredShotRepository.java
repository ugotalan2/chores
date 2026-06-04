package com.chores.api.repository;

import com.chores.api.entity.Chore;
import com.chores.api.entity.RequiredShot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RequiredShotRepository extends JpaRepository<RequiredShot, UUID> {

    // Find by chore, ordered by sortOrder
    List<RequiredShot> findByChoreOrderBySortOrder(Chore chore);
}
