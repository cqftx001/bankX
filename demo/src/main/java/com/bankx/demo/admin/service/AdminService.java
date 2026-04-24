package com.bankx.demo.admin.service;

import com.bankx.demo.admin.dto.AssignRoleRequest;
import com.bankx.demo.admin.vo.AdminUserVo;
import com.bankx.demo.user.UserProfileVo;

import java.util.List;
import java.util.UUID;

public interface AdminService {


    void deleteUser(UUID id);

    AdminUserVo unfreezeUserById(UUID id);

    AdminUserVo freezeUserById(UUID id);

    UserProfileVo getUserProfileById(UUID id);

    AdminUserVo getUserById(UUID id);

    List<AdminUserVo> getAllUsers();

    AdminUserVo assignRole(UUID id, AssignRoleRequest req);
}
