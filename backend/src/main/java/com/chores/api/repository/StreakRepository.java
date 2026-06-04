package com.chores.api.repository;

import com.chores.api.entity.Streak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StreakRepository extends JpaRepository<Streak, UUID> {

    // findById is all I need for now
}
