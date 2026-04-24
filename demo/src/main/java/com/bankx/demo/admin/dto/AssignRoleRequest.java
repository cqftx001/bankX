package com.bankx.demo.admin.dto;

import com.bankx.demo.common.enums.RoleEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Schema(description = "Assign role request")
public class AssignRoleRequest {

    @NotNull(message = "Role is required")
    @Schema(description = "Role name", example = "ROLE_ADMIN")
    private RoleEnum role;
}
