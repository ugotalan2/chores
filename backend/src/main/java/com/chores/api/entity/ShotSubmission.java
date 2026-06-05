package com.chores.api.entity;

import com.chores.api.enums.AiStatus;
import com.chores.api.enums.AiVerdict;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;

@Entity
@Table(name = "shot_submissions")
@AttributeOverride(name = "id", column = @Column(name = "submission_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ShotSubmission extends AuditedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instance_id", nullable = false)
    private DailyChoreInstance dailyChoreInstance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shot_id", nullable = false)
    private RequiredShot requiredShot;

    @Column(name = "photo_url",
        nullable = false,
        length = 500)
    private String photoUrl;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "captured_at",
        nullable = false,
        columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime capturedAt;

    @Column(name = "exif_timestamp", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime exifTimestamp;

    @Column(name = "exif_validated")
    private Boolean exifValidated;

    @Enumerated(EnumType.STRING)
    @Column(name = "ai_status",
        nullable = false,
        length = 20)
    private AiStatus aiStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "ai_verdict", length = 10)
    private AiVerdict aiVerdict;

    @Column(name = "ai_reason", columnDefinition = "TEXT")
    private String aiReason;

    @Column(name = "ai_processed_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime aiProcessedAt;

    @Builder.Default
    @Column(name = "disputed_by_kid", nullable = false)
    private Boolean disputedByKid = false;

    @Column(name = "disputed_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime disputedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "overridden_by")
    private User overriddenBy;

    @Column(name = "overridden_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime overriddenAt;

    @Column(name = "override_note", columnDefinition = "TEXT")
    private String overrideNote;

    @Builder.Default
    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber = 1;
}
