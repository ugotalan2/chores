package com.chores.api.entity;

import com.chores.api.enums.VerificationType;
import com.chores.api.enums.AiMode;
import com.chores.api.enums.AssignmentType;
import com.chores.api.enums.ChoreBlock;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "chores")
@AttributeOverride(name = "id", column = @Column(name = "chore_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Chore extends CreatedByAndSoftDeletableAndAuditedEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_type",
            nullable = false,
            length = 20)
    private VerificationType verificationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "ai_mode", length = 30)
    private AiMode aiMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "chore_block",
        nullable = false,
        length = 30)
    private ChoreBlock choreBlock;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_type",
        nullable = false,
        length = 30)
    private AssignmentType assignmentType;

    @Column(columnDefinition = "jsonb")
    private String instructions;

    @Column(columnDefinition = "jsonb")
    private String supplies;
}
