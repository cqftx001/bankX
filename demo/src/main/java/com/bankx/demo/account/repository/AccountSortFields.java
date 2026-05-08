package com.bankx.demo.account.repository;

import java.util.Set;

public class AccountSortFields {

    private AccountSortFields(){}

    public static final Set<String> ALLOWED = Set.of(
      "createdAt", "balance", "accountType", "accountStatus", "currency", "accountNumber"
    );
}
