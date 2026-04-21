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


@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Accounts API")
public class AccountController {

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
    public ResponseEntity<ResponseResult<List<AccountVo>>> geyMyAccounts(
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

    @PatchMapping("/{accountId}/freeze")
    @Operation(summary = "Freeze account")
    @PreAuthorize("hasAuthority('ACCOUNT:FREEZE')")
    public ResponseEntity<ResponseResult<AccountVo>> freezeAccount(
            @PathVariable UUID accountId,
            HttpServletRequest request
    ){

        String requestId = RequestUtils.getOrCreateRequestId(request);
        AccountVo vo = accountService.freezeAccount(accountId);
        return ResponseEntity.ok(ResponseResult.success(vo, requestId));
    }

    @PatchMapping("/{accountId}/unfreeze")
    @Operation(summary = "Unfreeze account")
    @PreAuthorize("hasAuthority('ACCOUNT:UNFREEZE')")
    public ResponseEntity<ResponseResult<AccountVo>> unfreezeAccount(
            @PathVariable UUID accountId,
            HttpServletRequest request
    ){

        String requestId = RequestUtils.getOrCreateRequestId(request);
        AccountVo vo = accountService.unfreezeAccount(accountId);
        return ResponseEntity.ok(ResponseResult.success(vo, requestId));
    }

    @PatchMapping("/{accountId}/close")
    @Operation(summary = "Close account")
    @PreAuthorize("hasAuthority('ACCOUNT:CLOSE')")
    public ResponseEntity<ResponseResult<AccountVo>> closeAccount(
            @PathVariable UUID accountId,
            HttpServletRequest request
    ){

        String requestId = RequestUtils.getOrCreateRequestId(request);
        AccountVo vo = accountService.closeAccount(accountId);
        return ResponseEntity.ok(ResponseResult.success(vo, requestId));
    }

    @PatchMapping("/{accountId}/unclose")
    @Operation(summary = "Unclose account")
    @PreAuthorize("hasAuthority('ACCOUNT:CLOSE')")
    public ResponseEntity<ResponseResult<AccountVo>> uncloseAccount(
            @PathVariable UUID accountId,
            HttpServletRequest request
    ){

        String requestId = RequestUtils.getOrCreateRequestId(request);
        AccountVo vo = accountService.uncloseAccount(accountId);
        return ResponseEntity.ok(ResponseResult.success(vo, requestId));
    }


}
