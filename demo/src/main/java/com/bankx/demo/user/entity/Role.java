package com.bankx.demo.user.entity;

import com.bankx.demo.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "roles")
@Schema(description = "Role")
public class Role extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    @Schema(description = "Name")
    private String name;

    @Column(nullable = false, length = 255)
    @Schema(description = "Description")
    private String description;

    @Column(nullable = false, length = 30)
    @Schema(description = "Enabled")
    private Boolean enabled;

    // —— Relationships ——————————————————————————————————————————————————————————
    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    @Schema(description = "UserRoles")
    private Set<UserRole> userRoles = new HashSet<>();

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    @Schema(description = "Permissions")
    private Set<RolePermission> rolePermissions = new HashSet<>();

}
