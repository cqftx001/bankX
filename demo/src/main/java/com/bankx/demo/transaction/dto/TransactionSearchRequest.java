package com.bankx.demo.transaction.dto;

import com.bankx.demo.common.base.BaseRequest;
import com.bankx.demo.common.enums.TransactionStatus;
import com.bankx.demo.common.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "Transaction search criteria - all fields are optional")
public class TransactionSearchRequest extends BaseRequest {

    @Schema(description = "Account ID")
    private UUID accountId;

    @Schema(description = "Start date", example = "2023-01-01")
    private LocalDate startDate;

    @Schema(description = "End date", example = "2023-01-31")
    private LocalDate endDate;

    @Schema(description = "Transaction type")
    private TransactionType type;

    @Schema(description = "Transaction status")
    private TransactionStatus status;

    @Schema(description = "Minimum amount")
    private BigDecimal minAmount;

    @Schema(description = "Maximum amount")
    private BigDecimal maxAmount;
}
