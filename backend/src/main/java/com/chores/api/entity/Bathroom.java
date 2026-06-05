package com.chores.api.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "bathrooms")
@AttributeOverride(name = "id", column = @Column(name = "bathroom_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Bathroom extends SoftDeletableAndAuditedEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;
}
