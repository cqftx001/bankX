package com.bankx.demo.account.repository;

import com.bankx.demo.account.dto.AccountSearchRequest;
import com.bankx.demo.account.entity.Account;
import com.bankx.demo.common.enums.AccountStatus;
import com.bankx.demo.common.enums.AccountType;
import com.bankx.demo.common.enums.CurrencyEnum;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AccountSpecifications {

    private AccountSpecifications(){}

    public static Specification<Account> notDeleted(){
        return (root, query, cb) -> cb.equal(root.get("deleted"), false);
    }

    public static Specification<Account> accountTypeEquals(AccountType type) {
        return (root, query, cb) -> cb.equal(root.get("accountType"), type);
    }

    public static Specification<Account> statusEquals(AccountStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Account> currencyEquals(CurrencyEnum currency) {
        return (root, query, cb) -> cb.equal(root.get("currency"), currency);
    }

    public static Specification<Account> balanceGreaterThanOrEqualTo(BigDecimal min) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("balance"), min);
    }

    public static Specification<Account> balanceLessThanOrEqualTo(BigDecimal max) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("balance"), max);
    }

    public static Specification<Account> belongsToUser(UUID userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Account> createdAfter(LocalDate date) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(
                root.get("createdAt"), date.atStartOfDay());
    }

    public static Specification<Account> createdBefore(LocalDate date) {
        return (root, query, cb) -> cb.lessThan(
                root.get("createdAt"), date.plusDays(1).atStartOfDay());
    }

    // -- builder --
    public static Specification<Account> buildSpecification(AccountSearchRequest request){
        List<Specification<Account>> specs = new ArrayList<>();

        specs.add(notDeleted());

        if(request != null){
            if (request.getAccountType() != null)       specs.add(accountTypeEquals(request.getAccountType()));
            if (request.getAccountStatus() != null)     specs.add(statusEquals(request.getAccountStatus()));
            if (request.getCurrency() != null)          specs.add(currencyEquals(request.getCurrency()));
            if (request.getMinBalance() != null)        specs.add(balanceGreaterThanOrEqualTo(request.getMinBalance()));
            if (request.getMaxBalance() != null)        specs.add(balanceLessThanOrEqualTo(request.getMaxBalance()));
            if (request.getUserId() != null)            specs.add(belongsToUser(request.getUserId()));
            if (request.getStartDate() != null)         specs.add(createdAfter(request.getStartDate()));
            if (request.getEndDate() != null)           specs.add(createdBefore(request.getEndDate()));
        }

        return combineWithAnd(specs);
    }

    private static Specification<Account> combineWithAnd(List<Specification<Account>> specs){
        Specification<Account> result = Specification.where(specs.get(0));
        for(Specification<Account> spec : specs){
            result = result.and(spec);
        }
        return result;
    }
}
