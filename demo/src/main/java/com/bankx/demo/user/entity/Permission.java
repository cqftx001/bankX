package com.bankx.demo.user.entity;

import com.bankx.demo.common.base.BaseEntity;
import com.bankx.demo.common.enums.ActionEnum;
import com.bankx.demo.common.enums.ResourceEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "permissions")
@Schema(description = "Permission")
public class Permission extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private ResourceEnum resource;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private ActionEnum action;

    @Column(nullable = false, length = 255)
    private String description;

    // —— Relationships ——————————————————————————————————————————————————————————
    @OneToMany(mappedBy = "permission", fetch = FetchType.LAZY)
    private Set<RolePermission> rolePermissions = new HashSet<>();

}
