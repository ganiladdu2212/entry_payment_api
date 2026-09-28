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

    @Column(name = "inserted_date", nullable = false, updatable = false)
    private LocalDateTime insertedDate;

    @PrePersist
    protected void setInsertedDate() {
        if (insertedDate == null) insertedDate = LocalDateTime.now(INDIA_ZONE);
    }
}
