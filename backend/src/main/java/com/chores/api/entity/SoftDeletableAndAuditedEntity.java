package com.chores.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.*;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
public class SoftDeletableAndAuditedEntity extends AuditedEntity {

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
