package com.bankx.demo.admin.service.impl;

import com.bankx.demo.admin.dto.AssignRoleRequest;
import com.bankx.demo.admin.service.AdminService;
import com.bankx.demo.admin.vo.AdminUserVo;
import com.bankx.demo.common.constant.SuperConstant;
import com.bankx.demo.common.enums.ErrorCode;
import com.bankx.demo.common.enums.UserStatus;
import com.bankx.demo.common.exception.BaseException;
import com.bankx.demo.security.Entity.Role;
import com.bankx.demo.security.Entity.UserRole;
import com.bankx.demo.security.model.CustomUserDetails;
import com.bankx.demo.security.properties.JwtProperties;
import com.bankx.demo.security.repository.RoleRepository;
import com.bankx.demo.user.UserProfileVo;
import com.bankx.demo.user.entity.User;
import com.bankx.demo.user.repository.UserRepository;
import com.bankx.demo.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final RoleRepository roleRepository;
    private final UserProfileService userProfileService;
    private final JwtProperties jwtProperties;


    // ══════════════════════════════════════════════
    // deleteUser
    // ══════════════════════════════════════════════
    @Override
    public void deleteUser(UUID id) {
        User user = findUserById(id);
        if(user.isDeleted()){
            throw new BaseException(ErrorCode.INVALID_REQUEST,
                    "User " + user.getProfile().getFullName() + " is already deleted");
        }

        redisTemplate.delete(SuperConstant.REDIS_TOKEN_PREFIX + id);
        user.setDeleted(true);
        userRepository.save(user);
    }

    // ══════════════════════════════════════════════
    // unfreezeUserById
    // ══════════════════════════════════════════════
    @Override
    @Transactional
    public AdminUserVo unfreezeUserById(UUID id) {
        User user = findUserById(id);

        if(user.getStatus() != UserStatus.FROZEN){
            throw new BaseException(ErrorCode.INVALID_REQUEST,
                    "Only FROZEN users can be unfrozen");
        }

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        redisTemplate.delete(SuperConstant.REDIS_FROZEN_USER_PREFIX + id);
        log.info("User unfrozen: userId={}", id);
        return toVo(user);
    }

    // ══════════════════════════════════════════════
    // freezeUserById
    // ══════════════════════════════════════════════
    @Override
    @Transactional
    public AdminUserVo freezeUserById(UUID id) {
        User user = findUserById(id);

        if(user.getStatus() != UserStatus.ACTIVE){
            throw new BaseException(ErrorCode.INVALID_REQUEST,
                    "Only ACTIVE users can be frozen");
        }

        user.setStatus(UserStatus.FROZEN);
        userRepository.save(user);

        // 强制下线
        redisTemplate.delete(SuperConstant.REDIS_TOKEN_PREFIX + id);
        redisTemplate.opsForValue().set(SuperConstant.REDIS_FROZEN_USER_PREFIX + id, "1",
                jwtProperties.getTtl(), TimeUnit.SECONDS);

        log.info("User frozen: userId={}", id);
        return toVo(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileVo getUserProfileById(UUID userId) {
        return userProfileService.getMyProfile(userId);
    }

    // ══════════════════════════════════════════════
    // getUserById
    // ══════════════════════════════════════════════
    @Override
    @Transactional(readOnly = true)
    public AdminUserVo getUserById(UUID id) {
        return toVo(findUserById(id));
    }

    // ══════════════════════════════════════════════
    // getAllUsers
    // ══════════════════════════════════════════════
    @Override
    @Transactional(readOnly = true)
    public List<AdminUserVo> getAllUsers() {
        return userRepository.findAll().stream().map(this::toVo).toList();
    }

    @Override
    @Transactional
    public AdminUserVo assignRole(UUID id, AssignRoleRequest req) {
        User user = findUserById(id);

        Role newRole = roleRepository.findByName(req.getRole())
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Role not found: " + req.getRole()));

        boolean alreadyHasRole = user.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole().getName() == req.getRole());

        if(alreadyHasRole){
            throw new BaseException(ErrorCode.DUPLICATE_REQUEST,
                    "User already has role: " + req.getRole());
        }

        // 清除旧角色 分配新角色
        user.getUserRoles().clear();

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(newRole);
        userRole.setAssignedAt(LocalDateTime.now());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null) throw new BaseException(ErrorCode.UNAUTHORIZED);
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        UUID adminId = userDetails.getUserId();

        userRole.setAssignedBy(adminId);
        user.getUserRoles().add(userRole);

        userRepository.save(user);
        // 强制下线
        redisTemplate.delete(SuperConstant.REDIS_TOKEN_PREFIX + id);

        log.info("User role assigned: userId={}, role={}", id, req.getRole());
        return toVo(user);
    }

    // -- helper --
    private User findUserById(UUID id){
        return userRepository.findById(id).orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
    }

    private AdminUserVo toVo(User user){
        return AdminUserVo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .userStatus(user.getStatus())
                .roles(user.getUserRoles().stream()
                        .map(ur -> ur.getRole().getName().name()).toList())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
