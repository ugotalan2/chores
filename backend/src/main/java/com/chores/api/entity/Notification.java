package com.chores.api.entity;

import com.chores.api.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.*;

@Entity
@Table(name = "notifications")
@AttributeOverride(name = "id", column = @Column(name = "notification_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Notification extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type",
        nullable = false,
        length = 30)
    private NotificationType notificationType;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "sent_to_user_ids",
        nullable = false,
        columnDefinition = "_uuid")
    private List<UUID> sentToUserIds;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instance_id")
    private DailyChoreInstance dailyChoreInstance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id")
    private ShotSubmission shotSubmission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id")
    private User fromUser;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "deep_link_path", length = 500)
    private String deepLinkPath;

    @Column(name = "sent_at",
        nullable = false,
        columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime sentAt;

    @Column(name = "read_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime readAt;
}
