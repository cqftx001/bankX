package com.bankx.demo.account.dto;

import com.bankx.demo.common.base.BaseDto;
import com.bankx.demo.common.enums.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Create account request")
public class CreateAccountRequest extends BaseDto {

    @NotNull(message = "Account type is required")
    private AccountType accountType;

}
