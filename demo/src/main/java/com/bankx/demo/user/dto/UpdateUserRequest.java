package com.bankx.demo.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Size(max = 100, message = "Email length must be <= 100")
    @Email(message = "Email format is invalid")
    private String email;

    @Size(max = 20, message = "Phone length must be <= 20")
    private String phone;

    @Size(max = 20, message = "Status length must be <= 20")
    private String status;

    private Boolean enabled;
}
