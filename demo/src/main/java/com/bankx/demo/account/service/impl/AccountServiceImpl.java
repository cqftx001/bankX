package com.bankx.demo.account.service.impl;

import com.bankx.demo.account.dto.CreateAccountRequest;
import com.bankx.demo.account.entity.Account;
import com.bankx.demo.account.repository.AccountRepository;
import com.bankx.demo.account.service.AccountService;
import com.bankx.demo.account.vo.AccountVo;
import com.bankx.demo.common.enums.AccountStatus;
import com.bankx.demo.common.enums.CurrencyEnum;
import com.bankx.demo.common.enums.ErrorCode;
import com.bankx.demo.common.exception.BaseException;
import com.bankx.demo.user.entity.User;
import com.bankx.demo.user.repository.UserRepository;
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
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AccountVo createAccount(UUID userId, CreateAccountRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        Account account = new Account();
        account.setUser(user);
        account.setAccountType(request.getAccountType());
        account.setBalance(BigDecimal.ZERO);
        account.setCurrency(CurrencyEnum.USD);
        account.setStatus(AccountStatus.ACTIVE);
        // Set account number;
        account.setAccountNumber(generateAccountNumber());
        accountRepository.save(account);

        log.info("Account created: accountId={}, userID={}, type={}",
                account.getId(), userId, request.getAccountType());
        return toVO(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountVo> getMyAccounts(UUID userId) {
        return accountRepository.findByUserId(userId)
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AccountVo getAccountById(UUID userId, UUID accountId) {
        Account account = findAccountById(accountId);

        // 普通用户只能查自己的账户
        if (!account.getUser().getId().equals(userId)) {
            throw new BaseException(ErrorCode.FORBIDDEN);
        }
        return toVO(account);
    }

    @Override
    @Transactional
    public AccountVo freezeAccount(UUID accountId) {
        Account account = findAccountById(accountId);

        if(account.getStatus() != AccountStatus.ACTIVE){
            throw new BaseException(ErrorCode.INVALID_REQUEST);
        }

        account.setStatus(AccountStatus.FROZEN);
        accountRepository.save(account);
        log.info("Account frozen: accountID={}", accountId);
        return toVO(account);
    }

    @Override
    @Transactional
    public AccountVo unfreezeAccount(UUID accountId) {
        Account account = findAccountById(accountId);

        if(account.getStatus() != AccountStatus.FROZEN){
            throw new BaseException(ErrorCode.INVALID_REQUEST, "Only FROZEN accounts can be unfrozen");
        }

        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);
        log.info("Account unfrozen: accountID={}", accountId);

        return toVO(account);
    }

    @Override
    @Transactional
    public AccountVo uncloseAccount(UUID accountId) {
        Account account = findAccountById(accountId);

        if(account.getStatus() != AccountStatus.CLOSED){
            throw new BaseException(ErrorCode.INVALID_REQUEST, "Only CLOSED accounts can be unfrozen");
        }

        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);
        log.info("Account unclosed: accountID={}", accountId);

        return toVO(account);
    }


    @Override
    @Transactional
    public AccountVo closeAccount(UUID accountId) {
        Account account = findAccountById(accountId);
        if(account.getStatus() != AccountStatus.ACTIVE){
            throw new BaseException(ErrorCode.INVALID_REQUEST,
                    "Only ACTIVE accounts can be closed");
        }

        if(account.getBalance().compareTo(BigDecimal.ZERO) > 0){
            throw new BaseException(ErrorCode.INVALID_REQUEST,
                    "Cannot close account with balance");
        }

        account.setStatus(AccountStatus.CLOSED);
        accountRepository.save(account);
        log.info("Account closed: accountID={}", accountId);

        return toVO(account);
    }

    // -- helper --
    private Account findAccountById(UUID accountId){
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new BaseException(ErrorCode.ACCOUNT_NOT_FOUND));
    }
    private String generateAccountNumber(){
        // Default bank code: BankX
        long sequence = accountRepository.getNextSequence();
        String base = String.format("%012d", sequence);

        int checkDigit = calculateCheckDigit(base);

        return "BKX" + base + checkDigit;
    }
    // LUHN check digit
    private int calculateCheckDigit(String base) {
        int sum = 0;
        boolean doubleDigit = true;
        for (int i = 0; i < base.length(); i++) {
            int digit = base.charAt(i) - '0';

            if(doubleDigit){
                digit *= 2;
                if(digit > 9){
                    digit -= 9;
                }
            }
            sum += digit;
            doubleDigit = !doubleDigit;
        }
        return (10 - (sum % 10)) % 10;
    }
    private AccountVo toVO(Account account){
        return AccountVo.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .build();
    }
}
