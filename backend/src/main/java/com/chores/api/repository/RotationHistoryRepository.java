package com.chores.api.repository;

import com.chores.api.entity.RotationHistory;
import com.chores.api.enums.RotationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RotationHistoryRepository extends JpaRepository<RotationHistory, UUID> {

    // find by rotation type
    List<RotationHistory> findByRotationTypeOrderByEffectiveDateDesc(RotationType rotationType);
}
