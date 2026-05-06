package com.bankx.demo.transaction.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
    *      "TRANSACTION:READ_OWN", "TRANSACTION:READ_ALL", "TRANSACTION:CREATE"
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/manager/transactions")
@Tag(name = "Manager Transaction", description = "Manager Transaction API")
public class ManagerTransactionController {

    // For further development, we can add APIs for manager to search transactions with filters and pagination, similar to TransactionController, but with more permissions
}
