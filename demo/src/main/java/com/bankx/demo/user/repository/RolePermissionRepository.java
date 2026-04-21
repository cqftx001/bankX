package com.bankx.demo.user.repository;

import com.bankx.demo.user.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, UUID> {

    boolean existsByRoleIdAndPermissionId(UUID roleId, UUID permissionId);

}
