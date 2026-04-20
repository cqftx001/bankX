package com.bankx.demo.user.entity;

import com.bankx.demo.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
@Table(
        name = "role_permissions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_role_permissions_role_permission",
                columnNames = {"role_id", "permission_id"}
        )
)
@Schema(description = "Role Permission Mapping")
public class RolePermission extends BaseEntity {

    // —— Relationships ——————————————————————————————————————————————————————————
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @Enumerated(EnumType.STRING)
    @JoinColumn(name = "role_id", nullable = false, updatable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permission_id", nullable = false, updatable = false)
    private Permission permission;
}
