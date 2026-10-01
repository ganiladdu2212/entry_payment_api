package com.enty.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class BaseEntity {
    private static final ZoneId INDIA_ZONE = ZoneId.of("Asia/Kolkata");

    protected BaseEntity() {
        // Required by JPA/Hibernate for entity construction.
    }

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    protected void setCreatedDate() {
        if (createdDate == null) createdDate = LocalDateTime.now(INDIA_ZONE);
    }
}
