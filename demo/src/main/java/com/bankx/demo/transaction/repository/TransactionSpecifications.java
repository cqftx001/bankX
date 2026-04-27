package com.bankx.demo.transaction.repository;

import com.bankx.demo.account.entity.Account;
import com.bankx.demo.common.enums.TransactionStatus;
import com.bankx.demo.common.enums.TransactionType;
import com.bankx.demo.transaction.dto.TransactionSearchRequest;
import com.bankx.demo.transaction.entity.Transaction;
import com.bankx.demo.user.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public final class TransactionSpecifications {

    private TransactionSpecifications(){}

    public static Specification<Transaction> buildSpecification(UUID userId, TransactionSearchRequest request){
        List<Specification<Transaction>> specs = new ArrayList<>();

        // 添加必要的安全条件
        specs.add(notDeleted());
        specs.add(belongsToUser(userId));

        if(request != null){
            if(request.getAccountId() != null) specs.add(involvesAccount(request.getAccountId()));
            if(request.getStartDate() != null) specs.add(startDateGreaterThanOrEqualTo(request.getStartDate()));
            if(request.getEndDate() != null) specs.add(endDateLessThanOrEqualTo(request.getEndDate()));
            if(request.getMaxAmount() != null) specs.add(maxAmountLessThanOrEqualTo(request.getMaxAmount()));
            if(request.getMinAmount() != null) specs.add(minAmountGreaterThanOrEqualTo(request.getMinAmount()));
            if(request.getType() != null) specs.add(transactionTypeEquals(request.getType()));
            if(request.getStatus() != null) specs.add(transactionStatusEquals(request.getStatus()));
        }

        return combineWithAnd(specs);
    }

    private static Specification<Transaction> combineWithAnd(List<Specification<Transaction>> specs){
        if(specs.isEmpty()) return null;
        return specs.stream().reduce(Specification::and).orElse(null);
    }

    /**
     * 拼接账号查询条件
     * @param accountId
     * @return
     */
    private static Specification<Transaction> involvesAccount(UUID accountId){
        return (root, query, cb) -> {
            Join<Transaction, Account> fromAccount = root.join("fromAccount", JoinType.LEFT);
            Join<Transaction, Account> toAccount = root.join("toAccount", JoinType.LEFT);

            return cb.or(
                    cb.equal(fromAccount.get("id"), accountId),
                    cb.equal(toAccount.get("id"), accountId)
            );
        };
    }

    /**
     * 拼接用户查询条件
     * @param userId
     * @return
     */
    private static Specification<Transaction> belongsToUser(UUID userId){
        return (root, query, cb) -> {
            // 显式用LEFT JOIN，因为Account是可选字段
            Join<Transaction, Account> fromAccount = root.join("fromAccount", JoinType.LEFT);
            Join<Transaction, Account> toAccount = root.join("toAccount", JoinType.LEFT);
            Join<Account, User> fromUserJoin = fromAccount.join("user", JoinType.LEFT);
            Join<Account, User> toUserJoin = toAccount.join("user", JoinType.LEFT);

            return cb.or(
                    cb.equal(fromUserJoin.get("id"), userId),
                    cb.equal(toUserJoin.get("id"), userId)
            );
        };
    }


    /**
     * 拼接删除状态查询条件
     * @return
     */
    private static Specification<Transaction> notDeleted(){
        return (root, query, cb) ->
                cb.isFalse(root.get("deleted"));
    }

    private static Specification<Transaction> startDateGreaterThanOrEqualTo(LocalDate startDate){
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("createdAt"), startDate.atStartOfDay());
    }

    private static Specification<Transaction> endDateLessThanOrEqualTo(LocalDate endDate){
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("createdAt"), endDate.atTime(LocalTime.MAX));
    }

    private static Specification<Transaction> transactionTypeEquals(TransactionType transactionType){
        return (root, query, cb) ->
                cb.equal(root.get("transactionType"), transactionType);
    }

    private static Specification<Transaction> transactionStatusEquals(TransactionStatus transactionStatus){
        return (root, query, cb) ->
                cb.equal(root.get("transactionStatus"), transactionStatus);
    }

    private static Specification<Transaction> minAmountGreaterThanOrEqualTo(BigDecimal minAmount){
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("amount"), minAmount);
    }

    private static Specification<Transaction> maxAmountLessThanOrEqualTo(BigDecimal maxAmount){
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("amount"), maxAmount);
    }

}
