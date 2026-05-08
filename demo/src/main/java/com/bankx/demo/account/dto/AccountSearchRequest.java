package com.bankx.demo.account.dto;

import com.bankx.demo.common.enums.AccountStatus;
import com.bankx.demo.common.enums.AccountType;
import com.bankx.demo.common.enums.CurrencyEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class AccountSearchRequest {

    private AccountType accountType;
    private AccountStatus accountStatus;
    private CurrencyEnum currency;
    private BigDecimal minBalance;
    private BigDecimal maxBalance;
    private UUID userId;
    private LocalDate startDate;
    private LocalDate endDate;

}
