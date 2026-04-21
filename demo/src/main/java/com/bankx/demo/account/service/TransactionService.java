package com.bankx.demo.account.service;

import com.bankx.demo.account.dto.DepositRequest;
import com.bankx.demo.account.dto.TransferRequest;
import com.bankx.demo.account.dto.WithDrawRequest;
import com.bankx.demo.account.vo.TransactionVo;

import java.util.List;
import java.util.UUID;

public interface TransactionService {

    TransactionVo deposit(UUID userId, DepositRequest request);

    TransactionVo withdraw(UUID userId, WithDrawRequest request);

    TransactionVo transfer(UUID userId, TransferRequest request);

    List<TransactionVo> getTransactions(UUID userId, UUID accountId);
}
