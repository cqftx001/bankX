package com.bankx.demo.account.vo;

import com.bankx.demo.common.enums.CurrencyEnum;
import com.bankx.demo.common.enums.TransactionStatus;
import com.bankx.demo.common.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@Schema(description = "Transaction VO")
public class TransactionVo {

    private UUID id;

    private String transactionNumber;

    private TransactionType type;

    private BigDecimal amount;

    private BigDecimal balanceAfter;

    private CurrencyEnum currency;

    private TransactionStatus status;

    private String description;

    private LocalDateTime createdAt;

}
