package com.bankx.demo.account.dto;

import com.bankx.demo.common.base.BaseRequest;
import com.bankx.demo.common.enums.CurrencyEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Withdraw request")
public class WithDrawRequest extends BaseRequest {

    @NotNull(message = "Source account id is required")
    private UUID fromAccountId;

    @NotNull
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull
    private CurrencyEnum currency;

    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;

    private String description;

}
