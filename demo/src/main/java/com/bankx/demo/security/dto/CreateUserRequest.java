package com.bankx.demo.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserRequest {

    @NotBlank(message = "Username cannot be blank")
    @Size(max = 50, message = "Username length must be <= 50")
    private String username;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email format is invalid")
    @Size(max = 100, message = "Email length must be <= 100")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, max = 100, message = "Password length must be between 6 and 100")
    private String password;

    @Size(max = 20, message = "Phone length must be <= 20")
    private String phone;
}