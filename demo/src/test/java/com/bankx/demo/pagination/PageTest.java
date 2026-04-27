package com.bankx.demo.pagination;

import com.bankx.demo.account.repository.AccountRepository;
import com.bankx.demo.common.base.PageResult;
import com.bankx.demo.common.enums.CurrencyEnum;
import com.bankx.demo.common.enums.ErrorCode;
import com.bankx.demo.common.enums.TransactionStatus;
import com.bankx.demo.common.enums.TransactionType;
import com.bankx.demo.common.exception.BaseException;
import com.bankx.demo.transaction.dto.TransactionSearchRequest;
import com.bankx.demo.transaction.entity.Transaction;
import com.bankx.demo.transaction.repository.TransactionRepository;
import com.bankx.demo.transaction.service.impl.TransactionServiceImpl;
import com.bankx.demo.transaction.vo.TransactionVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


public class PageTest {

    private TransactionRepository transactionRepository;
    private AccountRepository accountRepository;
    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setUp(){
        transactionRepository = mock(TransactionRepository.class);
        accountRepository = mock(AccountRepository.class);

        transactionService = new TransactionServiceImpl(transactionRepository, accountRepository);
    }

    @Test
    @DisplayName("search should return paginated transaction results")
    void search_shouldReturnPaginatedTransactionResults() {
        // Arrange
        UUID userId = UUID.randomUUID();

        TransactionSearchRequest request = new TransactionSearchRequest();

        Pageable pageable = PageRequest.of(
                0,
                2,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Transaction tx1 = buildTransaction(
                UUID.randomUUID(),
                "BKX001",
                TransactionType.DEPOSIT,
                new BigDecimal("100.00"),
                LocalDateTime.of(2026, 4, 25, 10, 0)
        );

        Transaction tx2 = buildTransaction(
                UUID.randomUUID(),
                "BKX002",
                TransactionType.WITHDRAW,
                new BigDecimal("50.00"),
                LocalDateTime.of(2026, 4, 25, 9, 30)
        );

        Page<Transaction> transactionPage = new PageImpl<>(
                List.of(tx1, tx2),
                pageable,
                5
        );

        when(transactionRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(transactionPage);

        // Act
        PageResult<TransactionVo> result =
                transactionService.search(userId, request, pageable);

        // Assert
        assertThat(result).isNotNull();

        /*
         * 根据你的 PageResult 字段名调整这里。
         * 常见写法可能是 getContent(), getItems(), getRecords(), getData()。
         */
        assertThat(result.getItems()).hasSize(2);

        assertThat(result.getItems().get(0).getTransactionNumber())
                .isEqualTo("BKX001");

        assertThat(result.getItems().get(0).getType())
                .isEqualTo(TransactionType.DEPOSIT);

        assertThat(result.getItems().get(0).getAmount())
                .isEqualByComparingTo("100.00");

        assertThat(result.getItems().get(1).getTransactionNumber())
                .isEqualTo("BKX002");

        /*
         * 根据你的 PageResult 字段名调整。
         * 如果 PageResult 里面叫 totalElements / total / page / size，
         * 改成对应 getter 即可。
         */
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(2);

        verify(transactionRepository, times(1))
                .findAll(any(Specification.class), eq(pageable));
    }

    // -- helper --
    private Transaction buildTransaction(UUID id,
                                         String transactionNumber,
                                         TransactionType type,
                                         BigDecimal amount,
                                         LocalDateTime createdAt){

        Transaction tx = new Transaction();

        tx.setId(id);
        tx.setTransactionNumber(transactionNumber);
        tx.setAmount(amount);
        tx.setTransactionType(type);
        tx.setCreatedAt(createdAt);

        return tx;
    }

}
