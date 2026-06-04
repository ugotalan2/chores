package com.chores.api.repository;

import com.chores.api.entity.DailyChoreInstance;
import com.chores.api.entity.User;
import com.chores.api.enums.ChoreBlock;
import com.chores.api.enums.ChoreStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface DailyChoreInstanceRepository extends JpaRepository<DailyChoreInstance, UUID> {

    // Find by User + date
    List<DailyChoreInstance> findByUserAndDate(User user, LocalDate date);

    // Find by User + Date + Status
    List<DailyChoreInstance> findByUserAndDateAndStatus(User user, LocalDate date, ChoreStatus choreStatus);

    // Find by user + date + status + block
    List<DailyChoreInstance> findByUserAndDateAndStatusAndChoreBlock(User user, LocalDate date, ChoreStatus choreStatus, ChoreBlock choreBlock);

    // Parent review queue - all instances awaiting review today
    List<DailyChoreInstance> findByStatusAndDate(ChoreStatus choreStatus, LocalDate date);

    // Carried over laundry check - daily generator needs this
    List<DailyChoreInstance> findByUserAndIsCarriedOverTrue(User user);
}
