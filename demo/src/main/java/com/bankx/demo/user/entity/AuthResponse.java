package com.bankx.demo.user.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
@Schema(description = "Successful authentication response")
public class AuthResponse {

    @Schema(description = "JWT token")
    private final String accessToken;

    // As defined in SuperConstant: "Bearer"
    @Schema(description = "JWT token type")
    private final String tokenType;

    @Schema(description = "JWT token expiration")
    private final long expiration;

    @Schema(description = "User ID")
    private final UUID userId;

    @Schema(description = "User email")
    private final String email;

    @Schema(description = "User roles")
    private final String roles;

}
