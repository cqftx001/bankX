package com.bankx.demo.common.config;

import com.bankx.demo.common.enums.ActionEnum;
import com.bankx.demo.common.enums.ResourceEnum;
import com.bankx.demo.common.enums.RoleEnum;
import com.bankx.demo.security.Entity.Permission;
import com.bankx.demo.security.Entity.Role;
import com.bankx.demo.security.Entity.RolePermission;
import com.bankx.demo.security.repository.PermissionRepository;
import com.bankx.demo.security.repository.RolePermissionRepository;
import com.bankx.demo.security.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * hardcode resources + permissions for each role
 * Would seed automatically while server start
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    // Permission definitions: resource → list of actions
    private static final Map<ResourceEnum, List<ActionEnum>> PERMISSION_DEFINITIONS = Map.of(
            ResourceEnum.ACCOUNT, List.of(ActionEnum.READ_OWN, ActionEnum.READ_ALL, ActionEnum.CREATE, ActionEnum.UPDATE,
                    ActionEnum.FREEZE, ActionEnum.UNFREEZE, ActionEnum.CLOSE
            ),
            ResourceEnum.TRANSACTION, List.of(
                    ActionEnum.CREATE, ActionEnum.READ_OWN, ActionEnum.READ_ALL, ActionEnum.REVERSE
            ),
            ResourceEnum.AUDIT_LOG, List.of(
                    ActionEnum.READ, ActionEnum.EXPORT
            ),
            ResourceEnum.USER, List.of(
                    ActionEnum.READ_OWN, ActionEnum.READ_ALL, ActionEnum.CREATE, ActionEnum.UPDATE, ActionEnum.DELETE,
                    ActionEnum.ASSIGN_ROLE, ActionEnum.FREEZE
            ),
            ResourceEnum.USER_PROFILE, List.of(
                    ActionEnum.READ_OWN, ActionEnum.UPDATE
            )
    );

    // Role -> which permissions it gets: "RESOURCE:ACTION"
    private static final Map<RoleEnum, List<String>> ROLE_PERMISSIONS = Map.of(
            RoleEnum.ROLE_CUSTOMER, List.of(
                    "ACCOUNT:READ_OWN", "ACCOUNT:CREATE",
                    "TRANSACTION:CREATE", "TRANSACTION:READ_OWN"
            ),
            RoleEnum.ROLE_TELLER, List.of(
                    "ACCOUNT:READ_OWN", "ACCOUNT:READ_ALL", "ACCOUNT:CREATE",
                    "TRANSACTION:CREATE", "TRANSACTION:READ_OWN", "TRANSACTION:READ_ALL"
            ),
            RoleEnum.ROLE_MANAGER, List.of(
                    "ACCOUNT:READ_OWN", "ACCOUNT:READ_ALL", "ACCOUNT:CREATE", "ACCOUNT:FREEZE",
                    "TRANSACTION:CREATE", "TRANSACTION:READ_OWN", "TRANSACTION:READ_ALL",
                    "AUDIT_LOG:READ"
            ),
            RoleEnum.ROLE_ADMIN, List.of(
                    // Account
                    "ACCOUNT:READ_OWN", "ACCOUNT:READ_ALL", "ACCOUNT:CREATE",
                    "ACCOUNT:FREEZE", "ACCOUNT:UNFREEZE", "ACCOUNT:CLOSE", "ACCOUNT:UPDATE",
                    // Transaction
                    "TRANSACTION:CREATE", "TRANSACTION:READ_OWN",
                    "TRANSACTION:READ_ALL", "TRANSACTION:REVERSE",
                    // Audit
                    "AUDIT_LOG:READ", "AUDIT_LOG:EXPORT",
                    // User 管理 — 细粒度，每个操作单独控制
                    "USER:READ_OWN", "USER:READ_ALL", "USER:CREATE",
                    "USER:UPDATE", "USER:DELETE", "USER:ASSIGN_ROLE", "USER:FREEZE",
                    // Profile
                    "USER_PROFILE:READ_OWN", "USER_PROFILE:UPDATE"
            )
    );

    private void seedPermissions() {
        PERMISSION_DEFINITIONS.forEach((resource, actions) ->
                actions.forEach(action -> {
                    permissionRepository.findByResourceAndAction(resource, action)
                            .orElseGet(() -> {
                                Permission p = new Permission();
                                p.setResource(resource);
                                p.setAction(action);
                                p.setDescription(resource + " — " + action);
                                permissionRepository.save(p);
                                log.debug("Created permission: {}:{}", resource, action);
                                return p;
                            });
                })
        );
    }

    private void seedRoles() {
        ROLE_PERMISSIONS.forEach((roleName, permKeys) -> {
            Role role = roleRepository.findByName(roleName)
                    .orElseGet(() -> {
                        Role r = new Role();
                        r.setName(roleName);
                        r.setDescription(roleName.name().replace("ROLE_", "").toLowerCase());
                        r.setEnabled(true);
                        return roleRepository.save(r);
                    });

            permKeys.forEach(key -> {
                String[] parts = key.split(":");
                ResourceEnum resource = ResourceEnum.valueOf(parts[0]);
                ActionEnum action = ActionEnum.valueOf(parts[1]);

                permissionRepository.findByResourceAndAction(resource, action)
                        .ifPresent(permission -> {
                            // 直接查 DB，不走 LAZY 加载
                            if (!rolePermissionRepository.existsByRoleIdAndPermissionId(
                                    role.getId(), permission.getId())) {
                                RolePermission rp = new RolePermission();
                                rp.setRole(role);
                                rp.setPermission(permission);
                                rolePermissionRepository.save(rp); // 直接存
                                log.debug("Linked {}:{} → {}", resource, action, roleName);
                            }
                        });
            });
        });
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Seeding data...");
        seedPermissions();
        seedRoles();
        log.info("DataInitializer completed");
    }
}
