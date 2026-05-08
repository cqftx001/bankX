package com.bankx.demo.transaction.service;

import com.bankx.demo.common.base.PageResult;
import com.bankx.demo.transaction.dto.*;
import com.bankx.demo.transaction.vo.TransactionVo;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TransactionService {

    TransactionVo deposit(UUID userId, DepositRequest request);

    TransactionVo withdraw(UUID userId, WithDrawRequest request);

    TransactionVo transfer(UUID userId, TransferRequest request);

    List<TransactionVo> getTransactions(UUID userId, UUID accountId);

    PageResult<TransactionVo> search(UUID userId, TransactionSearchRequest request, Pageable pageable);

    List<TransactionVo> getAllTransactions();

    TransactionVo reverseTransaction(UUID transactionId, UUID operatorUserId, String reason);

    PageResult<TransactionVo> getAllTransactions(TransactionSearchRequest request, Pageable pageable);
}
