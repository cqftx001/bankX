package com.bankx.demo.account.vo;

import com.bankx.demo.common.enums.AccountStatus;
import com.bankx.demo.common.enums.AccountType;
import com.bankx.demo.common.enums.CurrencyEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@Schema(description = "Account Info")
public class AccountVo {

    private UUID id;

    private String accountNumber;

    private AccountType accountType;

    private BigDecimal balance;

    private CurrencyEnum currency;

    private AccountStatus status;

    private LocalDateTime createdAt;

}
