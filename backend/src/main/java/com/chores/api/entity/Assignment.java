package com.chores.api.entity;

import com.chores.api.enums.AssignmentType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;

@Entity
@Table(name = "assignments")
@AttributeOverride(name = "id", column = @Column(name = "assignment_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Assignment extends CreatedByAndSoftDeletableAndAuditedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chore_id", nullable = false)
    private Chore chore;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_type",
        nullable = false,
        length = 30)
    private AssignmentType assignmentType;

    @Column(name = "rotation_start_date", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime rotationStartDate;

    @Column(name = "rotation_end_date", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime rotationEndDate;

    @Column(name = "current_room_index")
    private Integer currentRoomIndex;

    @Column(name = "current_task_index")
    private Integer currentTaskIndex;

    @Column(name = "last_rotated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime lastRotatedAt;

    @Builder.Default
    @Column(name = "is_summer_variation", nullable = false)
    private Boolean isSummerVariation = false;

    @Column(name = "summer_role_override", length = 200)
    private String summerRoleOverride;
}
