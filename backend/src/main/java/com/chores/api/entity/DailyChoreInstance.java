package com.chores.api.entity;

import com.chores.api.enums.ChoreBlock;
import com.chores.api.enums.DayType;
import com.chores.api.enums.ChoreStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.*;

@Entity
@Table(name = "daily_chore_instances")
@AttributeOverride(name = "id", column = @Column(name = "instance_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyChoreInstance extends AuditedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chore_id", nullable = false)
    private Chore chore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id")
    private Assignment assignment;

    @Column(nullable = false, columnDefinition = "DATE")
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_type",
        nullable = false,
        length = 20)
    private DayType dayType;

    @Enumerated(EnumType.STRING)
    @Column(name = "chore_block", nullable = false)
    private ChoreBlock choreBlock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChoreStatus status;

    @Column(name = "is_checked", nullable = false)
    private boolean isChecked = false;

    @Column(name = "checked_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime checkedAt;

    @Column(name = "started_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime startedAt;

    @Column(name = "completed_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime completedAt;

    @Column(name = "disputed_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime disputedAt;

    @Column(name = "dispute_note", columnDefinition = "TEXT")
    private String disputeNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_reviewed_by")
    private User parentReviewedBy;

    @Column(name = "parent_reviewed_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime parentReviewedAt;

    @Column(name = "parent_approved")
    private Boolean parentApproved;

    @Column(name = "parent_note", columnDefinition = "TEXT")
    private String parentNote;

    @Column(name = "is_carried_over", nullable = false)
    private boolean isCarriedOver = false;

    @Column(name = "original_date", columnDefinition = "DATE")
    private LocalDate originalDate;

    @Column(name = "affects_next_day_device", nullable = false)
    private boolean affectsNextDayDevice = false;
}
