package com.bankx.demo.user.entity;

import com.bankx.demo.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "user_roles")
@Schema(description = "User Role Mapping")
public class UserRole extends BaseEntity {

    @Schema(description = "assignedAt")
    private LocalDateTime assignedAt;

    @Schema(description = "assignedBy")
    private UUID assignedBy;

    // —— Relationships ——————————————————————————————————————————————————————————
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", referencedColumnName = "id", nullable = false)
    private Role role;
}
