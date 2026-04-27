package com.bankx.demo.transaction.repository;

import com.bankx.demo.transaction.entity.Transaction;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {

    @Query("""
    SELECT t FROM Transaction t
    WHERE t.fromAccount.id = :accountId
       OR t.toAccount.id = :accountId
    ORDER BY t.createdAt DESC
    """)
    List<Transaction> findAllByAccountId(@Param("accountId") UUID accountId);

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
}
