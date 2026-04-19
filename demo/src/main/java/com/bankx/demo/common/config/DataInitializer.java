package com.bankx.demo.common.config;

import com.bankx.demo.common.enums.ActionEnum;
import com.bankx.demo.common.enums.PermissionEnum;
import com.bankx.demo.common.enums.ResourceEnum;
import com.bankx.demo.common.enums.RoleEnum;
import com.bankx.demo.user.repository.PermissionRepository;
import com.bankx.demo.user.repository.RoleRepository;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

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
    private static final Map<RoleEnum, List<ActionEnum>> ROLE_PERMISSIONS = Map.of(
            RoleEnum.CUSTOMER, List.of(
                    "ACCOUNT:READ_OWN", "ACCOUNT:CREATE",
                    "TRANSACTION:CREATE", "TRANSACTION:READ_OWN"
            ),
            RoleEnum.TELLER, List.of(
                    "ACCOUNT:READ_OWN", "ACCOUNT:READ_ALL", "ACCOUNT:CREATE",
                    "TRANSACTION:CREATE", "TRANSACTION:READ_OWN", "TRANSACTION:READ_ALL"
            ),
            RoleEnum.MANAGER, List.of(
                    "ACCOUNT:READ_OWN", "ACCOUNT:READ_ALL", "ACCOUNT:CREATE", "ACCOUNT:FREEZE",
                    "TRANSACTION:CREATE", "TRANSACTION:READ_OWN", "TRANSACTION:READ_ALL",
                    "AUDIT_LOG:READ"
            ),
            RoleEnum.ADMIN, List.of(
                    "ACCOUNT:READ_OWN", "ACCOUNT:READ_ALL", "ACCOUNT:CREATE", "ACCOUNT:FREEZE",
                    "TRANSACTION:CREATE", "TRANSACTION:READ_OWN", "TRANSACTION:READ_ALL",
                    "AUDIT_LOG:READ", "USER:MANAGE"
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
                        r.setDescription(roleName.replace("ROLE_", "").toLowerCase());
                        r.setEnabled(true);
                        roleRepository.save(r);
                        log.debug("Created role: {}", roleName);
                        return r;
                    });

            // Assign permissions that aren't already linked
            permKeys.forEach(key -> {
                String[] parts = key.split(":");
                permissionRepository.findByResourceAndAction(parts[0], parts[1])
                        .ifPresent(permission -> {
                            boolean alreadyLinked = role.getRolePermissions().stream()
                                    .anyMatch(rp -> rp.getPermission().equals(permission));
                            if (!alreadyLinked) {
                                RolePermission rp = new RolePermission();
                                rp.setRole(role);
                                rp.setPermission(permission);
                                role.getRolePermissions().add(rp);
                                roleRepository.save(role);
                            }
                        });
            });
        });
    }
}
