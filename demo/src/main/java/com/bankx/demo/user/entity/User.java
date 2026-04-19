package com.bankx.demo.user.entity;

import com.bankx.demo.common.base.BaseEntity;
import com.bankx.demo.common.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "users")
@Schema(description = "User")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    @Schema(description = "username")
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    @Schema(description = "email")
    private String email;

    @Column(nullable = false, length = 255)
    @Schema(description = "passwordHash")
    private String passwordHash;

    @Column(nullable = false, length = 20)
    @Schema(description = "phone")
    private String phone;

    /**
     * User status, e.g.  ACTIVE / INACTIVE / LOCKED / SUSPENDED / CLOSED.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Schema(description = "user status")
    private UserStatus status;

    @Schema(description = "last login time")
    private LocalDateTime lastLoginAt;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "user roles")
    private Set<UserRole> userRoles = new HashSet<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private UserProfile profile;


}
