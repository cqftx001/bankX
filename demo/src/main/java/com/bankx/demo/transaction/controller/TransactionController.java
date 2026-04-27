package com.bankx.demo.transaction.controller;

import com.bankx.demo.common.base.PageResult;
import com.bankx.demo.transaction.dto.DepositRequest;
import com.bankx.demo.transaction.dto.TransactionSearchRequest;
import com.bankx.demo.transaction.dto.TransferRequest;
import com.bankx.demo.transaction.dto.WithDrawRequest;
import com.bankx.demo.transaction.service.TransactionService;
import com.bankx.demo.transaction.vo.TransactionVo;
import com.bankx.demo.common.base.ResponseResult;
import com.bankx.demo.common.utils.RequestUtils;
import com.bankx.demo.security.model.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@RestController
@Tag(name = "Transactions", description = "Deposit, withdraw and transfer")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    @Operation(summary = "Search current user's transactions with filters and pagination")
    public ResponseEntity<ResponseResult<PageResult<TransactionVo>>> search(
            @ParameterObject TransactionSearchRequest searchRequest,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request){

        String requestId = RequestUtils.getOrCreateRequestId(request);

        PageResult<TransactionVo> result = transactionService.search(
                userDetails.getUserId(),
                searchRequest,
                pageable
        );
        return ResponseEntity.ok().body(ResponseResult.success(result, requestId));
    }

    @PostMapping("/deposit")
    @Operation(summary = "Deposit funds into an account")
    @PreAuthorize("hasAuthority('TRANSACTION:CREATE')")
    public ResponseEntity<ResponseResult<TransactionVo>> deposit(
            @Valid @RequestBody DepositRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ){

        String requestId = RequestUtils.getOrCreateRequestId(request);
        TransactionVo vo = transactionService.deposit(userDetails.getUserId(), req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseResult.success(vo, requestId));
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw funds from an account")
    @PreAuthorize("hasAuthority('TRANSACTION:CREATE')")

    public ResponseEntity<ResponseResult<TransactionVo>> withdraw(
            @Valid @RequestBody WithDrawRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ){

        String requestId = RequestUtils.getOrCreateRequestId(request);
        TransactionVo vo = transactionService.withdraw(userDetails.getUserId(), req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseResult.success(vo, requestId));
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer funds between accounts")
    @PreAuthorize("hasAuthority('TRANSACTION:CREATE')")
    public ResponseEntity<ResponseResult<TransactionVo>> transfer(
            @Valid @RequestBody TransferRequest req,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request) {

        String requestId = RequestUtils.getOrCreateRequestId(request);
        TransactionVo vo = transactionService.transfer(userDetails.getUserId(), req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseResult.success(vo, requestId));
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Get transaction history for an account")
    @PreAuthorize("hasAuthority('TRANSACTION:READ_OWN')")
    public ResponseEntity<ResponseResult<List<TransactionVo>>> getTransactions(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request) {

        String requestId = RequestUtils.getOrCreateRequestId(request);
        List<TransactionVo> txList = transactionService
                .getTransactions(userDetails.getUserId(), accountId);
        return ResponseEntity.ok(ResponseResult.success(txList, requestId));
    }
}
