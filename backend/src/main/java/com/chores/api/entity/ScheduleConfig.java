package com.chores.api.entity;

import com.chores.api.enums.ScheduleType;
import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "schedule_config")
@AttributeOverride(name = "id", column = @Column(name = "config_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleConfig extends CreatedByAndSoftDeletableAndAuditedEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type",
        nullable = false,
        length = 30)
    private ScheduleType scheduleType;

    @Column(name = "screen_time_gate_enabled")
    private boolean screenTimeGateEnabled = true;

    @Column(name = "screen_time_gate_hour")
    private Integer screenTimeGateHour = 14;

    @Type(ListArrayType.class)
    @Column(name = "active_blocks",
        nullable = false,
        columnDefinition = "_text")
    private List<String> activeBlocks;

    @Column(name = "saturday_block_active", nullable = false)
    private boolean saturdayBlockActive = true;

    @Column(name = "sunday_kitchen_only", nullable = false)
    private boolean sundayKitchenOnly = true;

    @Column(name = "sunday_enforced", nullable = false)
    private boolean sundayEnforced = false;

    @Column(name = "valid_from", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime validFrom;

    @Column(name = "valid_to", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime validTo;
}
