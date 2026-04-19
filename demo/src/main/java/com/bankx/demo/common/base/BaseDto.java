package com.bankx.demo.common.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public abstract class BaseDto implements Serializable {

    @Schema(description = "Id")
    private UUID id;

    @Schema(description = "Created at")
    private LocalDateTime createdAt;

}
