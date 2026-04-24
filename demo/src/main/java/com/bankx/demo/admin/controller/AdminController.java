package com.bankx.demo.admin.controller;

import com.bankx.demo.admin.dto.AssignRoleRequest;
import com.bankx.demo.admin.service.AdminService;
import com.bankx.demo.admin.vo.AdminUserVo;
import com.bankx.demo.common.base.ResponseResult;
import com.bankx.demo.common.utils.RequestUtils;
import com.bankx.demo.user.UserProfileVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin API")
public class AdminController{

    private final AdminService adminService;

    @GetMapping("/users")
    @Operation(summary = "Get all users")
    public ResponseEntity<ResponseResult<List<AdminUserVo>>> getAllUsers(
            HttpServletRequest request
    ){
        String requestId = RequestUtils.getOrCreateRequestId(request);
        List<AdminUserVo> list = adminService.getAllUsers();
        return ResponseEntity.ok().body(ResponseResult.success(list, requestId));
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user by id")
    public ResponseEntity<ResponseResult<AdminUserVo>> getUserById(
            @PathVariable UUID id,
            HttpServletRequest request
    ){
        String requestId = RequestUtils.getOrCreateRequestId(request);
        AdminUserVo vo = adminService.getUserById(id);
        return ResponseEntity.ok().body(ResponseResult.success(vo, requestId));
    }

    @GetMapping("/users/{id}/profile")
    @Operation(summary = "Get user profile by id")
    public ResponseEntity<ResponseResult<UserProfileVo>> getUserProfileById(
            @PathVariable UUID id,
            HttpServletRequest request
    ){
        String requestId = RequestUtils.getOrCreateRequestId(request);
        UserProfileVo vo = adminService.getUserProfileById(id);
        return ResponseEntity.ok().body(ResponseResult.success(vo, requestId));
    }

    @PatchMapping("/users/{id}/freeze")
    @Operation(summary = "Freeze user by id")
    public ResponseEntity<ResponseResult<AdminUserVo>> freezeUserById(
            @PathVariable UUID id,
            HttpServletRequest request
    ){
        String requestId = RequestUtils.getOrCreateRequestId(request);
        AdminUserVo vo = adminService.freezeUserById(id);
        return ResponseEntity.ok().body(ResponseResult.success(vo, requestId));
    }

    @PatchMapping("/users/{id}/unfreeze")
    @Operation(summary = "Unfreeze user by id")
    public ResponseEntity<ResponseResult<AdminUserVo>> unfreezeUserById(
            @PathVariable UUID id,
            HttpServletRequest request
    ){
        String requestId = RequestUtils.getOrCreateRequestId(request);
        AdminUserVo vo = adminService.unfreezeUserById(id);
        return ResponseEntity.ok().body(ResponseResult.success(vo, requestId));
    }

    @PatchMapping("/users/{id}/role")
    @Operation(summary = "Assign role to user")
    public ResponseEntity<ResponseResult<AdminUserVo>> assignRole(
            @PathVariable UUID id,
            @Valid @RequestBody AssignRoleRequest req,
            HttpServletRequest request) {

        String requestId = RequestUtils.getOrCreateRequestId(request);
        AdminUserVo vo = adminService.assignRole(id, req);
        return ResponseEntity.ok(ResponseResult.success(vo, requestId));
    }

    @PatchMapping("/users/{id}/delete")
    @Operation(summary = "Soft delete user by id")
    public ResponseEntity<ResponseResult<Void>> deleteUserById(
            @PathVariable UUID id,
            HttpServletRequest request
    ){
        String requestId = RequestUtils.getOrCreateRequestId(request);
        adminService.deleteUser(id);
        return ResponseEntity.ok().body(ResponseResult.success(requestId));
    }
}
