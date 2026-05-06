package com.bankx.demo.transaction.controller;

import com.bankx.demo.common.base.ResponseResult;
import com.bankx.demo.common.utils.RequestUtils;
import com.bankx.demo.security.model.CustomUserDetails;
import com.bankx.demo.transaction.dto.ReverseTransactionRequest;
import com.bankx.demo.transaction.service.TransactionService;
import com.bankx.demo.transaction.vo.TransactionVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/*
    *              "TRANSACTION:CREATE", "TRANSACTION:READ_OWN",
    *              "TRANSACTION:READ_ALL", "TRANSACTION:REVERSE",
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/transactions")
@Tag(name = "Admin Transaction", description = "Admin Transaction API")
public class AdminTransactionController {

    private final TransactionService transactionService;
    @PostMapping("/{transactionId}/reverse")
    @PreAuthorize("hasAuthority('TRANSACTION:REVERSE')")
    public ResponseEntity<ResponseResult<TransactionVo>> reverseTransaction(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID transactionId,
            @Valid @RequestBody ReverseTransactionRequest reverseTransactionRequest,
            HttpServletRequest request
    ){

        UUID operatorId = userDetails.getUserId();
        String requestID = RequestUtils.getOrCreateRequestId(request);
        TransactionVo transaction = transactionService.reverseTransaction(transactionId, operatorId, reverseTransactionRequest.reason());
        return ResponseEntity.ok(ResponseResult.success(transaction, requestID));
    }

}
