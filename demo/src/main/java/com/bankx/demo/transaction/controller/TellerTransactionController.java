package com.bankx.demo.transaction.controller;

import com.bankx.demo.common.base.ResponseResult;
import com.bankx.demo.common.utils.RequestUtils;
import com.bankx.demo.transaction.service.TransactionService;
import com.bankx.demo.transaction.vo.TransactionVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/*
    *      "TRANSACTION:READ_OWN", "TRANSACTION:READ_ALL", "TRANSACTION:CREATE"
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/teller/transactions")
@Tag(name = "Teller Transaction Controller", description = "Teller Transaction APIs")
public class TellerTransactionController {

    private final TransactionService transactionService;

    @GetMapping
    @Operation(summary = "Get all transactions for teller")
    public ResponseEntity<ResponseResult<List<TransactionVo>>> getAllTransactions(
            HttpServletRequest request
    ){
        String requestId = RequestUtils.getOrCreateRequestId(request);
        List<TransactionVo> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(ResponseResult.success(transactions, requestId));
    }

    // for further  development, we can add more APIs for teller to search transactions with filters and pagination, similar to TransactionController, but with more permissions
}
