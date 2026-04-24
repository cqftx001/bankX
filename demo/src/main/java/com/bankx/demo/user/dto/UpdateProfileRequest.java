package com.bankx.demo.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Update profile request")
public class UpdateProfileRequest {

    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    @Size(max = 20)
    private String phone;

    @Past(message = "Date of birth must be in the past")
    private LocalDate birthDate;

    @Size(max = 100)
    private String addressLine1;

    @Size(max = 100)
    private String addressLine2;

    @Size(max = 50)
    private String city;

    @Size(max = 50)
    private String state;

    @Size(max = 10)
    private String zipCode;

    @Size(max = 50)
    private String country;

}
