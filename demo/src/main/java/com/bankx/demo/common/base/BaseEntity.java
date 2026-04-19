package com.bankx.demo.common.base;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "Base Entity Class")
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "UUID")
    private UUID id;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    @Schema(description = "Created Date")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    @Schema(description = "Updated Date")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by",updatable = false)
    @Schema(description = "Created By")
    private UUID createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    @Schema(description = "Updated By")
    private UUID updatedBy;

    @Column(name = "deleted")
    @Schema(description = "Deleted")
    private boolean deleted = false;

}
