package com.chores.api.repository;

import com.chores.api.entity.DailyChoreInstance;
import com.chores.api.entity.ShotSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ShotSubmissionRepository extends JpaRepository<ShotSubmission, UUID> {

    // find by instance
    List<ShotSubmission> findByDailyChoreInstance(DailyChoreInstance dailyChoreInstance);

    // find disputed submissions
    List<ShotSubmission> findByDisputedByKidTrue();

    // all disputed submissions for a specific instance
    List<ShotSubmission> findByDailyChoreInstanceAndDisputedByKidTrue(DailyChoreInstance dailyChoreInstance);
}
