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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/*
 *      "ACCOUNT:READ_OWN", "ACCOUNT:READ_ALL", "ACCOUNT:CREATE"
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/teller/accounts")
@Tag(name = "Teller Account Controller", description = "Teller Account APIs")
public class TellerAccountController {

    private final AccountService accountService;

    @GetMapping
    @Operation(summary = "Get all accounts for teller")
    @PreAuthorize("hasAuthority('ACCOUNT:READ_ALL')")
    public ResponseEntity<ResponseResult<List<AccountVo>>> getAllAccounts(
            HttpServletRequest request
            ){

        String requestId = RequestUtils.getOrCreateRequestId(request);
        List<AccountVo> accounts = accountService.getAllAccounts();

        return ResponseEntity.ok(ResponseResult.success(accounts, requestId));
    }

}
