package com.chores.api.entity;

import com.chores.api.enums.ScheduleType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "schedule_config")
@AttributeOverride(name = "id", column = @Column(name = "config_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ScheduleConfig extends CreatedByAndSoftDeletableAndAuditedEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type",
        nullable = false,
        length = 30)
    private ScheduleType scheduleType;

    @Builder.Default
    @Column(name = "screen_time_gate_enabled")
    private Boolean screenTimeGateEnabled = true;

    @Builder.Default
    @Column(name = "screen_time_gate_hour")
    private Integer screenTimeGateHour = 14;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "active_blocks",
        nullable = false,
        columnDefinition = "_text")
    private List<String> activeBlocks;

    @Builder.Default
    @Column(name = "saturday_block_active", nullable = false)
    private Boolean saturdayBlockActive = true;

    @Builder.Default
    @Column(name = "sunday_kitchen_only", nullable = false)
    private Boolean sundayKitchenOnly = true;

    @Builder.Default
    @Column(name = "sunday_enforced", nullable = false)
    private Boolean sundayEnforced = false;

    @Column(name = "valid_from", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime validFrom;

    @Column(name = "valid_to", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime validTo;
}
