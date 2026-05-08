package com.bankx.demo.account.service;

import com.bankx.demo.account.dto.AccountSearchRequest;
import com.bankx.demo.account.dto.CreateAccountRequest;
import com.bankx.demo.account.entity.Account;
import com.bankx.demo.account.vo.AccountVo;
import com.bankx.demo.common.base.PageResult;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface AccountService {

    AccountVo createAccount(UUID userId, CreateAccountRequest request);

    List<AccountVo> getMyAccounts(UUID userId);

    AccountVo getAccountById(UUID userId, UUID accountId);

    AccountVo freezeAccount(UUID accountId);

    AccountVo unfreezeAccount(UUID accountId);

    AccountVo uncloseAccount(UUID accountId);

    AccountVo closeAccount(UUID accountId);

    List<AccountVo> getAllAccounts();

    PageResult<AccountVo> getAllAccounts(AccountSearchRequest request, Pageable pageable);
}
