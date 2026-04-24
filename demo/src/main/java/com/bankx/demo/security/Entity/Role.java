package com.bankx.demo.security.Entity;

import com.bankx.demo.common.base.BaseEntity;
import com.bankx.demo.common.enums.RoleEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "roles")
@Schema(description = "Role")
public class Role extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 100)
    private RoleEnum name;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false, length = 30)
    private Boolean enabled;

    // —— Relationships ——————————————————————————————————————————————————————————
    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    private Set<UserRole> userRoles = new HashSet<>();

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    private Set<RolePermission> rolePermissions = new HashSet<>();

}
