package com.bankx.demo.account.controller;

import com.bankx.demo.account.dto.CreateAccountRequest;
import com.bankx.demo.account.service.AccountService;
import com.bankx.demo.account.vo.AccountVo;
import com.bankx.demo.common.base.ResponseResult;
import com.bankx.demo.common.utils.RequestUtils;
import com.bankx.demo.security.model.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 *                     "ACCOUNT:READ_OWN", "ACCOUNT:CREATE",
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/me/accounts")
@Tag(name = "Account Controller", description = "Self Accounts APIs")
public class SelfAccountController {

    private final AccountService accountService;


    @PostMapping
    @Operation(summary = "Create account")
    @PreAuthorize("hasAuthority('ACCOUNT:CREATE')")
    public ResponseEntity<ResponseResult<AccountVo>> createAccount(
            @Valid @RequestBody CreateAccountRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
            ){
        String requestId = RequestUtils.getOrCreateRequestId(request);
        AccountVo vo = accountService.createAccount(userDetails.getUserId(), req);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseResult.success(vo, requestId));
    }

    @GetMapping
    @Operation(summary = "Get my accounts")
    @PreAuthorize("hasAuthority('ACCOUNT:READ_OWN')")
    public ResponseEntity<ResponseResult<List<AccountVo>>> getMyAccounts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ){

        String requestId = RequestUtils.getOrCreateRequestId(request);
        List<AccountVo> accounts = accountService.getMyAccounts(userDetails.getUserId());
        return ResponseEntity.ok(ResponseResult.success(accounts, requestId));
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Get account by id")
    @PreAuthorize("hasAuthority('ACCOUNT:READ_OWN')")
    public ResponseEntity<ResponseResult<AccountVo>> getAccountById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID accountId,
            HttpServletRequest request
    ){

        String requestId = RequestUtils.getOrCreateRequestId(request);
        AccountVo vo = accountService.getAccountById(userDetails.getUserId(), accountId);
        return ResponseEntity.ok(ResponseResult.success(vo, requestId));
    }

}
