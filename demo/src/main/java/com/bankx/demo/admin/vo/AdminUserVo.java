package com.bankx.demo.admin.vo;

import com.bankx.demo.common.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@Schema(description = "管理员用户信息")
public class AdminUserVo {

    private UUID id;
    private String username;
    private String email;
    private UserStatus userStatus;
    private List<String> roles;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;

}