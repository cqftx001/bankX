package com.bankx.demo.account.controller;

import com.bankx.demo.account.dto.AccountSearchRequest;
import com.bankx.demo.account.service.AccountService;
import com.bankx.demo.account.vo.AccountVo;
import com.bankx.demo.common.base.PageResult;
import com.bankx.demo.common.base.ResponseResult;
import com.bankx.demo.common.utils.RequestUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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
    @Operation(summary = "Get all accounts for pagination")
    @PreAuthorize("hasAuthority('ACCOUNT:READ_ALL')")
    public ResponseEntity<ResponseResult<PageResult<AccountVo>>> getAllAccountsByPage(
            @ParameterObject AccountSearchRequest searchRequest,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            HttpServletRequest request
    ){
        String requestId = RequestUtils.getOrCreateRequestId(request);
        PageResult<AccountVo> result = accountService.getAllAccounts(searchRequest, pageable);
        return ResponseEntity.ok(ResponseResult.success(result, requestId));
     }

}
