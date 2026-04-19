package com.bankx.demo.common.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(description = "Base Audit VO")
public class BaseAuditVO implements Serializable {

    @Schema(description = "Primary Key")
    private UUID id;

    @Schema(description = "Created Time")
    private LocalDateTime createdAt;

    @Schema(description = "Updated Time")
    private LocalDateTime updatedAt;

    @Schema(description = "Created By")
    private UUID createdBy;

    @Schema(description = "Updated By")
    private UUID updatedBy;

}