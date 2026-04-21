package com.bankx.demo.account.service.Impl;

import com.bankx.demo.account.dto.DepositRequest;
import com.bankx.demo.account.dto.TransferRequest;
import com.bankx.demo.account.dto.WithDrawRequest;
import com.bankx.demo.account.entity.Account;
import com.bankx.demo.account.entity.Transaction;
import com.bankx.demo.account.repository.AccountRepository;
import com.bankx.demo.account.repository.TransactionRepository;
import com.bankx.demo.account.service.TransactionService;
import com.bankx.demo.account.vo.TransactionVo;
import com.bankx.demo.common.enums.*;
import com.bankx.demo.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public TransactionVo deposit(UUID userId, DepositRequest request) {
        // 幂等检查
        checkIdempotency(request.getIdempotencyKey());

        Account toAccount = findActiveAccount(request.getToAccountId());
        validateAccountOwner(toAccount, userId);

        // 更新余额
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));
        accountRepository.save(toAccount);

        // 生成TransactionId
        Transaction transaction = buildTransaction(
                TransactionType.DEPOSIT,
                request.getAmount(),
                toAccount.getBalance(),
                request.getIdempotencyKey(),
                request.getDescription(),
                null,
                toAccount,
                request.getCurrency()
        );
        transactionRepository.save(transaction);

        log.info("Deposit completed: txId={}, accountId={}, amount={}",
                transaction.getId(), toAccount.getId(), request.getAmount());
        return toVO(transaction, toAccount.getId());
    }

    @Override
    @Transactional
    public TransactionVo withdraw(UUID userId, WithDrawRequest request) {
        // 幂等检查
        checkIdempotency(request.getIdempotencyKey());
        Account fromAccount = findActiveAccount(request.getFromAccountId());
        validateAccountOwner(fromAccount, userId);

        if(fromAccount.getBalance().compareTo(request.getAmount()) < 0){
            throw new BaseException(ErrorCode.INSUFFICIENT_FUNDS,
                    "Insufficient funds: available " + fromAccount.getBalance());
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        accountRepository.save(fromAccount);

        Transaction tx = buildTransaction(
                TransactionType.WITHDRAW,
                request.getAmount(),
                fromAccount.getBalance(),
                request.getIdempotencyKey(),
                request.getDescription(),
                fromAccount,
                null,
                request.getCurrency()
        );

        transactionRepository.save(tx);

        log.info("Withdrawal completed: txId={}, accountId={}, amount={}",
                tx.getId(), fromAccount.getId(), request.getAmount());
        return toVO(tx, fromAccount.getId());
    }

    @Override
    @Transactional
    public TransactionVo transfer(UUID userId, TransferRequest request) {
        checkIdempotency(request.getIdempotencyKey());

        if(request.getFromAccountId().equals(request.getToAccountId())){
            throw new BaseException(ErrorCode.INVALID_REQUEST,
                    "Cannot transfer to the same account");
        }

        Account fromAccount = findActiveAccount(request.getFromAccountId());
        Account toAccount = findActiveAccount(request.getToAccountId());
        validateAccountOwner(fromAccount, userId);

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BaseException(ErrorCode.INSUFFICIENT_FUNDS,
                    "Insufficient funds: available " + fromAccount.getBalance());
        }

        // update two accounts at the same time
        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        Transaction tx = buildTransaction(
                TransactionType.TRANSFER,
                request.getAmount(),
                toAccount.getBalance(),
                request.getIdempotencyKey(),
                request.getDescription(),
                fromAccount,
                toAccount,
                request.getCurrency()
        );

        transactionRepository.save(tx);

        log.info("Transfer completed: txId={}, from={}, to={}, amount={}",
                tx.getId(), fromAccount.getId(), toAccount.getId(), request.getAmount());
        return toVO(tx, fromAccount.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionVo> getTransactions(UUID userId, UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BaseException(ErrorCode.ACCOUNT_NOT_FOUND));

        validateAccountOwner(account, userId);

        return transactionRepository.findAllByAccountId(accountId)
                .stream()
                .map(tx -> toVO(tx, accountId))
                .toList();
    }

    // -- helper --
    private void checkIdempotency(String idempotencyKey) {
        if (transactionRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
            throw new BaseException(ErrorCode.DUPLICATE_REQUEST,
                    "Duplicate transaction: idempotency key already used");
        }
    }

    private Account findActiveAccount(UUID accountId){
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if(account.getStatus() != AccountStatus.ACTIVE){
            throw new BaseException(ErrorCode.INVALID_REQUEST,
                    "Account is not active: " + account.getStatus());
        }
        return account;
    }

    private void validateAccountOwner(Account account, UUID userId){
        if(!account.getUser().getId().equals(userId)){
            throw new BaseException(ErrorCode.FORBIDDEN,
                    "You do not have access to this account");
        }
    }

    private String generateTransactionNumber(){
        return "BKX" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 12).toUpperCase();
    }

    private Transaction buildTransaction(TransactionType  type,
                                         BigDecimal amount,
                                         BigDecimal balanceAfter,
                                         String idempotencyKey,
                                         String description,
                                         Account fromAccount,
                                         Account toAccount,
                                         CurrencyEnum  currency){

        Transaction transaction = new Transaction();
        transaction.setTransactionType(type);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setCurrency(currency);
        transaction.setTransactionStatus(TransactionStatus.COMPLETED);
        transaction.setIdempotencyKey(idempotencyKey);
        transaction.setDescription(description);
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setTransactionNumber(generateTransactionNumber());
        return transaction;
    }

    private TransactionVo toVO(Transaction tx, UUID viewerAccountId) {
        // 从 viewer 的视角决定 amount 的正负
        boolean isOutgoing = tx.getFromAccount() != null
                && tx.getFromAccount().getId().equals(viewerAccountId);
        BigDecimal displayAmount = isOutgoing
                ? tx.getAmount().negate()
                : tx.getAmount();

        return TransactionVo.builder()
                .id(tx.getId())
                .transactionNumber(tx.getTransactionNumber())
                .type(tx.getTransactionType())
                .amount(displayAmount)
                .balanceAfter(tx.getBalanceAfter())
                .currency(tx.getCurrency())
                .status(tx.getTransactionStatus())
                .description(tx.getDescription())
                .createdAt(tx.getCreatedAt())
                .build();
    }

}
