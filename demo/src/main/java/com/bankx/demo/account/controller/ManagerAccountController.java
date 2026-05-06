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

/*
 *      "ACCOUNT:READ_OWN", "ACCOUNT:READ_ALL", "ACCOUNT:CREATE", "ACCOUNT:FREEZE"
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/manager/accounts")
@Tag(name = "Manager Account Controller", description = "Manager Account APIs")
public class ManagerAccountController {

    private final AccountService accountService;
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
}
