package com.bankx.demo.admin;

import com.bankx.demo.admin.dto.AssignRoleRequest;
import com.bankx.demo.admin.service.impl.AdminServiceImpl;
import com.bankx.demo.admin.vo.AdminUserVo;
import com.bankx.demo.common.constant.SuperConstant;
import com.bankx.demo.common.enums.ErrorCode;
import com.bankx.demo.common.enums.RoleEnum;
import com.bankx.demo.common.enums.UserStatus;
import com.bankx.demo.common.exception.BaseException;
import com.bankx.demo.security.Entity.Role;
import com.bankx.demo.security.Entity.UserRole;
import com.bankx.demo.security.model.CustomUserDetails;
import com.bankx.demo.security.properties.JwtProperties;
import com.bankx.demo.security.repository.RoleRepository;
import com.bankx.demo.user.entity.User;
import com.bankx.demo.user.repository.UserRepository;
import com.bankx.demo.user.service.UserProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminServiceImpl Unit Tests")
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private UserProfileService userProfileService;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;
    @Mock private JwtProperties jwtProperties;

    @InjectMocks
    private AdminServiceImpl adminService;

    private UUID userId;
    private UUID adminId;
    private User activeUser;
    private User frozenUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        adminId = UUID.randomUUID();
        when(jwtProperties.getTtl()).thenReturn(3600000L);

        // 准备 ACTIVE 用户
        activeUser = new User();
        activeUser.setStatus(UserStatus.ACTIVE);
        activeUser.setUserRoles(new HashSet<>());

        // 准备 FROZEN 用户
        frozenUser = new User();
        frozenUser.setStatus(UserStatus.FROZEN);
        frozenUser.setUserRoles(new HashSet<>());

        // Mock Redis valueOperations
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Mock SecurityContext — Admin 登录状态
        mockSecurityContext(adminId);
    }

    // ══════════════════════════════════════════════
    // getAllUsers
    // ══════════════════════════════════════════════
    @Nested
    @DisplayName("getAllUsers")
    class GetAllUsers {

        @Test
        @DisplayName("✅ 正常返回用户列表")
        void getAllUsers_success() {
            when(userRepository.findAll()).thenReturn(List.of(activeUser));

            List<AdminUserVo> result = adminService.getAllUsers();

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(userRepository).findAll();
        }

        @Test
        @DisplayName("✅ 没有用户时返回空列表")
        void getAllUsers_empty() {
            when(userRepository.findAll()).thenReturn(List.of());

            List<AdminUserVo> result = adminService.getAllUsers();

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    // ══════════════════════════════════════════════
    // getUserById
    // ══════════════════════════════════════════════
    @Nested
    @DisplayName("getUserById")
    class GetUserById {

        @Test
        @DisplayName("✅ 正常返回用户")
        void getUserById_success() {
            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(activeUser));

            AdminUserVo result = adminService.getUserById(userId);

            assertNotNull(result);
            verify(userRepository).findById(userId);
        }

        @Test
        @DisplayName("❌ 用户不存在抛出 USER_NOT_FOUND")
        void getUserById_notFound_throwsException() {
            when(userRepository.findById(userId))
                    .thenReturn(Optional.empty());

            BaseException ex = assertThrows(BaseException.class,
                    () -> adminService.getUserById(userId));

            assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
        }
    }

    // ══════════════════════════════════════════════
    // freezeUserById
    // ══════════════════════════════════════════════
    @Nested
    @DisplayName("freezeUserById")
    class FreezeUser {

        @Test
        @DisplayName("✅ ACTIVE 用户冻结成功")
        void freezeUser_activeUser_success() {
            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(activeUser));

            adminService.freezeUserById(userId);

            assertEquals(UserStatus.FROZEN, activeUser.getStatus());
            verify(userRepository).save(activeUser);
            verify(redisTemplate).delete(
                    SuperConstant.REDIS_TOKEN_PREFIX + userId);
            verify(valueOperations).set(
                    eq(SuperConstant.REDIS_FROZEN_USER_PREFIX + userId),
                    eq("1"),
                    eq(3600000L),
                    eq(TimeUnit.SECONDS)
            );
        }

        @Test
        @DisplayName("❌ FROZEN 用户再次冻结抛出 INVALID_REQUEST")
        void freezeUser_alreadyFrozen_throwsException() {
            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(frozenUser));

            BaseException ex = assertThrows(BaseException.class,
                    () -> adminService.freezeUserById(userId));

            assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("❌ 用户不存在抛出 USER_NOT_FOUND")
        void freezeUser_notFound_throwsException() {
            when(userRepository.findById(userId))
                    .thenReturn(Optional.empty());

            assertThrows(BaseException.class,
                    () -> adminService.freezeUserById(userId));
        }
    }

    // ══════════════════════════════════════════════
    // unfreezeUserById
    // ══════════════════════════════════════════════
    @Nested
    @DisplayName("unfreezeUserById")
    class UnfreezeUser {

        @Test
        @DisplayName("✅ FROZEN 用户解冻成功")
        void unfreezeUser_frozenUser_success() {
            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(frozenUser));

            adminService.unfreezeUserById(userId);

            assertEquals(UserStatus.ACTIVE, frozenUser.getStatus());
            verify(userRepository).save(frozenUser);
            verify(redisTemplate).delete(
                    SuperConstant.REDIS_FROZEN_USER_PREFIX + userId);
        }

        @Test
        @DisplayName("❌ ACTIVE 用户解冻抛出 INVALID_REQUEST")
        void unfreezeUser_notFrozen_throwsException() {
            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(activeUser));

            BaseException ex = assertThrows(BaseException.class,
                    () -> adminService.unfreezeUserById(userId));

            assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
        }

        @Test
        @DisplayName("❌ 用户不存在抛出 USER_NOT_FOUND")
        void unfreezeUser_notFound_throwsException() {
            when(userRepository.findById(userId))
                    .thenReturn(Optional.empty());

            assertThrows(BaseException.class,
                    () -> adminService.unfreezeUserById(userId));
        }
    }

    // ══════════════════════════════════════════════
    // assignRole
    // ══════════════════════════════════════════════
    @Nested
    @DisplayName("assignRole")
    class AssignRole {

        private Role tellerRole;
        private AssignRoleRequest req;

        @BeforeEach
        void setUp() {
            tellerRole = new Role();
            tellerRole.setName(RoleEnum.ROLE_TELLER);

            req = new AssignRoleRequest();
            req.setRole(RoleEnum.ROLE_TELLER);
        }

        @Test
        @DisplayName("✅ 正常分配新角色")
        void assignRole_success() {
            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(activeUser));
            when(roleRepository.findByName(RoleEnum.ROLE_TELLER))
                    .thenReturn(Optional.of(tellerRole));

            adminService.assignRole(userId, req);

            verify(userRepository).save(activeUser);
            verify(redisTemplate).delete(
                    SuperConstant.REDIS_TOKEN_PREFIX + userId);
        }

        @Test
        @DisplayName("❌ 重复分配同一角色抛出 DUPLICATE_REQUEST")
        void assignRole_duplicateRole_throwsException() {
            UserRole existingRole = new UserRole();
            existingRole.setRole(tellerRole);
            activeUser.setUserRoles(new HashSet<>(Set.of(existingRole)));

            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(activeUser));
            when(roleRepository.findByName(RoleEnum.ROLE_TELLER))
                    .thenReturn(Optional.of(tellerRole));

            BaseException ex = assertThrows(BaseException.class,
                    () -> adminService.assignRole(userId, req));

            assertEquals(ErrorCode.DUPLICATE_REQUEST, ex.getErrorCode());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("❌ 角色不存在抛出 RESOURCE_NOT_FOUND")
        void assignRole_roleNotFound_throwsException() {
            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(activeUser));
            when(roleRepository.findByName(any()))
                    .thenReturn(Optional.empty());

            BaseException ex = assertThrows(BaseException.class,
                    () -> adminService.assignRole(userId, req));

            assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("❌ 用户不存在抛出 USER_NOT_FOUND")
        void assignRole_userNotFound_throwsException() {
            when(userRepository.findById(userId))
                    .thenReturn(Optional.empty());

            assertThrows(BaseException.class,
                    () -> adminService.assignRole(userId, req));
        }
    }

    // ══════════════════════════════════════════════
    // deleteUser
    // ══════════════════════════════════════════════
    @Nested
    @DisplayName("deleteUser")
    class DeleteUser {

        @Test
        @DisplayName("✅ 正常软删除用户")
        void deleteUser_success() {
            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(activeUser));

            adminService.deleteUser(userId);

            assertTrue(activeUser.isDeleted());
            verify(userRepository).save(activeUser);
            verify(redisTemplate).delete(
                    SuperConstant.REDIS_TOKEN_PREFIX + userId);
        }

        @Test
        @DisplayName("❌ 用户不存在抛出 USER_NOT_FOUND")
        void deleteUser_notFound_throwsException() {
            when(userRepository.findById(userId))
                    .thenReturn(Optional.empty());

            assertThrows(BaseException.class,
                    () -> adminService.deleteUser(userId));
        }
    }

    // ══════════════════════════════════════════════
    // helper
    // ══════════════════════════════════════════════
    private void mockSecurityContext(UUID adminId) {
        CustomUserDetails adminDetails = mock(CustomUserDetails.class);
        when(adminDetails.getUserId()).thenReturn(adminId);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(adminDetails);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);
    }
}