package com.bankx.demo.account.entity;

import com.bankx.demo.common.base.BaseEntity;
import com.bankx.demo.common.enums.CurrencyEnum;
import com.bankx.demo.common.enums.TransactionStatus;
import com.bankx.demo.common.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "transactions")
@Schema(description = "Transaction Entity")
public class Transaction extends BaseEntity {

    @Column(name = "transaction_number", nullable = false, unique = true, length = 15)
    private String transactionNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private TransactionType transactionType;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private TransactionStatus transactionStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 10)
    private CurrencyEnum currency;

    @Column(name = "balance_after", nullable = false, precision = 19, scale = 4)
    private BigDecimal balanceAfter;

    @Column(name = "description", nullable = true, length = 255)
    private String description;

    @Column(name = "idempotency_key", nullable = true, length = 100)
    private String idempotencyKey;

    //  Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id", nullable = false)
    private Account fromAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id", nullable = false)
    private Account toAccount;
}
