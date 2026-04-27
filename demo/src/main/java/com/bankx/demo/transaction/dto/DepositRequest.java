package com.bankx.demo.transaction.dto;

import com.bankx.demo.common.base.BaseDto;
import com.bankx.demo.common.enums.CurrencyEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Deposit request")
public class DepositRequest extends BaseDto {

    @NotNull(message = "To account id is required")
    private UUID toAccountId;

    @NotNull
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull
    private CurrencyEnum currency;

    private String description;

    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;

}
