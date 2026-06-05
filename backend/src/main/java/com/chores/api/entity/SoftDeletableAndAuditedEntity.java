package com.chores.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@NoArgsConstructor
@SuperBuilder
public class SoftDeletableAndAuditedEntity extends AuditedEntity {

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
