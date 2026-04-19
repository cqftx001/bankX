package com.bankx.demo.user.repository;

import com.bankx.demo.common.enums.ActionEnum;
import com.bankx.demo.common.enums.ResourceEnum;
import com.bankx.demo.user.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    Optional<Permission> findByResourceAndAction(ResourceEnum resource, ActionEnum action);

}
