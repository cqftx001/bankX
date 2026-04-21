package com.bankx.demo.account.repository;

import com.bankx.demo.account.entity.Account;
import com.bankx.demo.common.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    List<Account> findByUserId(UUID userId);

    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    List<Account> findByUserIdAndStatus(UUID userId, AccountStatus  status);

    @Query(value = "SELECT nextval('account_sequence')", nativeQuery = true)
    Long getNextSequence();

}
