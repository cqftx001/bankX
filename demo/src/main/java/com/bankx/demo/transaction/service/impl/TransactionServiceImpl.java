package com.bankx.demo.transaction.service.impl;

import com.bankx.demo.account.entity.Account;
import com.bankx.demo.account.repository.AccountRepository;
import com.bankx.demo.common.base.PageResult;
import com.bankx.demo.common.enums.*;
import com.bankx.demo.common.exception.BaseException;
import com.bankx.demo.common.utils.PageableUtils;
import com.bankx.demo.transaction.dto.DepositRequest;
import com.bankx.demo.transaction.dto.TransactionSearchRequest;
import com.bankx.demo.transaction.dto.TransferRequest;
import com.bankx.demo.transaction.dto.WithDrawRequest;
import com.bankx.demo.transaction.entity.Transaction;
import com.bankx.demo.transaction.repository.TransactionRepository;
import com.bankx.demo.transaction.repository.TransactionSortFields;
import com.bankx.demo.transaction.repository.TransactionSpecifications;
import com.bankx.demo.transaction.service.TransactionService;
import com.bankx.demo.transaction.vo.TransactionVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    // ════════════════════════════════════════════════════════════
    // paginated search
    // ════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public PageResult<TransactionVo> search(UUID userId,
                                            TransactionSearchRequest req,
                                            Pageable pageable) {

        // Step 1: validate sort fields against whitelist (rejects unsafe sorts with 400)
        Pageable safePageable = PageableUtils.sanitize(pageable, TransactionSortFields.ALLOWED);

        // Step 2: assemble the spec - belongsToUser + notDeleted are baked in
        Specification<Transaction> spec = TransactionSpecifications.buildSpecification(userId, req);

        // Step 3: execute paginated query
        Page<Transaction> page = transactionRepository.findAll(spec, safePageable);

        // Step 4: wrap into our API contract
        return PageResult.from(page).map(this::toVO);
    }

    // ════════════════════════════════════════════════════════════
    // Existing: deposit / withdraw / transfer (refactored)
    // ════════════════════════════════════════════════════════════

    @Override
    @Transactional
    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3, backoff = @Backoff(delay = 100))
    public TransactionVo deposit(UUID userId, DepositRequest request) {
        Optional<TransactionVo> existing = checkIdempotency(request.getIdempotencyKey());
        if (existing.isPresent()) return existing.get();

        Account toAccount = findActiveAccount(request.getToAccountId());
        validateAccountOwner(toAccount, userId);

        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));
        accountRepository.save(toAccount);

        Transaction tx = buildTransaction(
                TransactionType.DEPOSIT,
                request.getAmount(),
                toAccount.getBalance(),
                request.getIdempotencyKey(),
                request.getDescription(),
                null,
                toAccount,
                request.getCurrency()
        );
        transactionRepository.save(tx);

        log.info("Deposit completed: txId={}, accountId={}, amount={}",
                tx.getId(), toAccount.getId(), request.getAmount());
        return toVO(tx);
    }

    @Override
    @Transactional
    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3, backoff = @Backoff(delay = 100))
    public TransactionVo withdraw(UUID userId, WithDrawRequest request) {
        Optional<TransactionVo> existing = checkIdempotency(request.getIdempotencyKey());
        if (existing.isPresent()) return existing.get();

        Account fromAccount = findActiveAccount(request.getFromAccountId());
        validateAccountOwner(fromAccount, userId);

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
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
        return toVO(tx);
    }

    @Override
    @Transactional
    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3, backoff = @Backoff(delay = 100))
    public TransactionVo transfer(UUID userId, TransferRequest request) {
        Optional<TransactionVo> existing = checkIdempotency(request.getIdempotencyKey());
        if (existing.isPresent()) return existing.get();

        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new BaseException(ErrorCode.INVALID_REQUEST, "Cannot transfer to the same account");
        }

        // Avoid deadlock by always locking accounts in deterministic order
        UUID firstId  = request.getFromAccountId().compareTo(request.getToAccountId()) < 0
                ? request.getFromAccountId() : request.getToAccountId();
        UUID secondId = request.getFromAccountId().compareTo(request.getToAccountId()) < 0
                ? request.getToAccountId() : request.getFromAccountId();

        Account firstAccount  = findActiveAccount(firstId);
        Account secondAccount = findActiveAccount(secondId);

        Account fromAccount = firstId.equals(request.getFromAccountId()) ? firstAccount : secondAccount;
        Account toAccount   = firstId.equals(request.getToAccountId())   ? firstAccount : secondAccount;

        validateAccountOwner(fromAccount, userId);

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BaseException(ErrorCode.INSUFFICIENT_FUNDS,
                    "Insufficient funds: available " + fromAccount.getBalance());
        }

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
        return toVO(tx);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionVo> getTransactions(UUID userId, UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BaseException(ErrorCode.ACCOUNT_NOT_FOUND));
        validateAccountOwner(account, userId);

        return transactionRepository.findAllByAccountId(accountId)
                .stream()
                .map(this::toVO)
                .toList();
    }

    // ─── Helpers ────────────────────────────────────────────

    private Optional<TransactionVo> checkIdempotency(String idempotencyKey) {
        if (idempotencyKey == null) return Optional.empty();
        return transactionRepository.findByIdempotencyKey(idempotencyKey).map(this::toVO);
    }

    private Account findActiveAccount(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BaseException(ErrorCode.ACCOUNT_NOT_FOUND, "Account not found"));
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BaseException(ErrorCode.INVALID_REQUEST,
                    "Account is not active: " + account.getStatus());
        }
        return account;
    }

    private void validateAccountOwner(Account account, UUID userId) {
        if (!account.getUser().getId().equals(userId)) {
            throw new BaseException(ErrorCode.FORBIDDEN, "You do not have access to this account");
        }
    }

    private String generateTransactionNumber() {
        return "BKX" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 12).toUpperCase();
    }

    private Transaction buildTransaction(TransactionType type,
                                         BigDecimal amount,
                                         BigDecimal balanceAfter,
                                         String idempotencyKey,
                                         String description,
                                         Account fromAccount,
                                         Account toAccount,
                                         CurrencyEnum currency) {
        Transaction tx = new Transaction();
        tx.setTransactionType(type);
        tx.setAmount(amount);
        tx.setBalanceAfter(balanceAfter);
        tx.setCurrency(currency);
        tx.setTransactionStatus(TransactionStatus.COMPLETED);
        tx.setIdempotencyKey(idempotencyKey);
        tx.setDescription(description);
        tx.setFromAccount(fromAccount);
        tx.setToAccount(toAccount);
        tx.setTransactionNumber(generateTransactionNumber());
        return tx;
    }

    private TransactionVo toVO(Transaction tx) {
        return TransactionVo.builder()
                .id(tx.getId())
                .transactionNumber(tx.getTransactionNumber())
                .type(tx.getTransactionType())
                .status(tx.getTransactionStatus())
                .amount(tx.getAmount())
                .currency(tx.getCurrency())
                .balanceAfter(tx.getBalanceAfter())
                .fromAccountId(tx.getFromAccount() != null ? tx.getFromAccount().getId() : null)
                .fromAccountNumber(tx.getFromAccount() != null ? tx.getFromAccount().getAccountNumber() : null)
                .toAccountId(tx.getToAccount() != null ? tx.getToAccount().getId() : null)
                .toAccountNumber(tx.getToAccount() != null ? tx.getToAccount().getAccountNumber() : null)
                .description(tx.getDescription())
                .createdAt(tx.getCreatedAt())
                .build();
    }
}