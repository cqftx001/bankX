package com.bankx.demo.transaction.vo;

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

    @Schema(description = "Transaction primary key")
    private UUID id;

    @Schema(description = "Human-readable transaction number", example = "BKX1A2B3C4D5E6F")
    private String transactionNumber;

    @Schema(description = "Transaction type", example = "TRANSFER")
    private TransactionType type;

    @Schema(description = "Transaction status", example = "COMPLETED")
    private TransactionStatus status;

    @Schema(description = "Absolute transaction amount", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "Currency", example = "USD")
    private CurrencyEnum currency;

    @Schema(description = "Account balance after this transaction", example = "950.00")
    private BigDecimal balanceAfter;

    @Schema(description = "Source account ID (null for DEPOSIT)")
    private UUID fromAccountId;

    @Schema(description = "Source account number (null for DEPOSIT)", example = "ACC-1234567890")
    private String fromAccountNumber;

    @Schema(description = "Destination account ID (null for WITHDRAW)")
    private UUID toAccountId;

    @Schema(description = "Destination account number (null for WITHDRAW)", example = "ACC-9876543210")
    private String toAccountNumber;

    @Schema(description = "Transaction description / memo")
    private String description;

    @Schema(description = "Transaction creation timestamp")
    private LocalDateTime createdAt;
}