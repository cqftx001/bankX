package com.bankx.demo.transaction.repository;

import org.springframework.data.domain.Sort;

import java.util.Set;

public final class TransactionSortFields {

    private TransactionSortFields(){}

    public static final Set<String> ALLOWED = Set.of(
            "createdAt",
            "amount",
            "transactionType",
            "transactionStatus"
    );

    // Default sort applied when client does not specify
    public static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");
}
