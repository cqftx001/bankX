package com.bankx.demo.account.controller;

import com.bankx.demo.account.service.AccountService;
import com.bankx.demo.account.vo.AccountVo;
import com.bankx.demo.common.base.ResponseResult;
import com.bankx.demo.common.utils.RequestUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 *      "ACCOUNT:READ_OWN", "ACCOUNT:READ_ALL", "ACCOUNT:CREATE",
 *      "ACCOUNT:FREEZE", "ACCOUNT:UNFREEZE", "ACCOUNT:CLOSE", "ACCOUNT:UPDATE",
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/accounts")
@Tag(name = "Admin Account Controller", description = "Admin Account APIs")
public class AdminAccountController {

    private final AccountService accountService;

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

    // soft delete account, just mark it as closed, not actually delete the record in database
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
