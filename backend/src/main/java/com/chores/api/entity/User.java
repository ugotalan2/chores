package com.chores.api.entity;

import com.chores.api.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "users")
@AttributeOverride(name = "id", column = @Column(name = "user_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class User extends CreatedByAndSoftDeletableAndAuditedEntity {

    @Column(name = "display_name",
        nullable = false,
        length = 100)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    // --- CHILD only ---

    private Integer age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_bathroom_id")
    private Bathroom assignedBathroom;

    @Column(name = "laundry_day_of_week")
    private Integer laundryDayOfWeek;

    // --- PARENT only ---

    @Column(unique = true, length = 255)
    private String email;

    @Column(name = "clerk_user_id",
        unique = true,
        length = 255)
    private String clerkUserId;

    @Column(name = "fcm_token", length = 500)
    private String fcmToken;

    @Column(name = "is_primary_admin")
    private Boolean isPrimaryAdmin;
}
