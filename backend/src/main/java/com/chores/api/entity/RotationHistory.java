package com.chores.api.entity;

import com.chores.api.enums.RotationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "rotation_history")
@AttributeOverride(name = "id", column = @Column(name = "history_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RotationHistory extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "rotation_type",
        nullable = false,
        length = 30)
    private RotationType rotationType;

    @Column(name = "effective_date",
        nullable = false,
        columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime effectiveDate;

    @Column(name = "previous_assignments", columnDefinition = "jsonb")
    private String previousAssignments;

    @Column(name = "new_assignments", columnDefinition = "jsonb")
    private String newAssignments;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "triggered_by", nullable = false)
    private User triggeredBy;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
