package com.bankx.demo.transaction.controller;

import com.bankx.demo.common.base.PageResult;
import com.bankx.demo.common.base.ResponseResult;
import com.bankx.demo.common.utils.RequestUtils;
import com.bankx.demo.transaction.dto.TransactionSearchRequest;
import com.bankx.demo.transaction.service.TransactionService;
import com.bankx.demo.transaction.vo.TransactionVo;
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
    *      "TRANSACTION:READ_OWN", "TRANSACTION:READ_ALL", "TRANSACTION:CREATE"
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/teller/transactions")
@Tag(name = "Teller Transaction Controller", description = "Teller Transaction APIs")
public class TellerTransactionController {

    private final TransactionService transactionService;

    @GetMapping
    @Operation(summary = "Get all transactions (paginated)")
    @PreAuthorize("hasAuthority('TRANSACTION:READ_ALL')")
    public ResponseEntity<ResponseResult<PageResult<TransactionVo>>> getAllTransactions(
            @ParameterObject TransactionSearchRequest searchRequest,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            HttpServletRequest request
    ) {
        String requestId = RequestUtils.getOrCreateRequestId(request);
        PageResult<TransactionVo> result = transactionService.getAllTransactions(searchRequest, pageable);
        return ResponseEntity.ok(ResponseResult.success(result, requestId));
    }

    // for further  development, we can add more APIs for teller to search transactions with filters and pagination, similar to TransactionController, but with more permissions
}
