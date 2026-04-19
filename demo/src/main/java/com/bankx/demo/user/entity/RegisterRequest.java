package com.bankx.demo.user.entity;

import com.bankx.demo.common.base.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "New customer registration request")
public class RegisterRequest extends BaseRequest {

    // ── Credentials (→ User table) ────────────────────────────

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
    @Schema(example = "john_doe")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100)
    @Schema(example = "john@example.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    @Schema(example = "Str0ngP@ss!")
    private String password;

    // ── Personal info (→ UserProfile table) ───────────────────

    @NotBlank(message = "First name is required")
    @Size(max = 50)
    @Schema(example = "John")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50)
    @Schema(example = "Doe")
    private String lastName;

    @Size(max = 20, message = "Phone must be 20 characters or less")
    @Schema(example = "+12025551234")
    private String phone;

    @Past(message = "Date of birth must be in the past")
    @Schema(example = "1990-05-15")
    private LocalDate dateOfBirth;

    @Size(max = 100)
    @Schema(example = "123 Main St")
    private String addressLine1;

    @Size(max = 100)
    @Schema(example = "Apt 4B")
    private String addressLine2;

    @Size(max = 50)
    @Schema(example = "Los Angeles")
    private String city;

    @Size(min = 2, max = 2, message = "State must be a 2-letter code")
    @Schema(example = "CA")
    private String state;

    @Size(max = 10)
    @Schema(example = "90210")
    private String zipCode;

    @Size(min = 2, max = 2, message = "Country must be a 2-letter ISO code")
    @Schema(example = "US")
    private String country;
}