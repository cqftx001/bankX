package com.bankx.demo.common.utils;

import com.bankx.demo.common.enums.ErrorCode;
import com.bankx.demo.common.exception.BaseException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class PageableUtils {

    private PageableUtils(){}
    public static Pageable sanitize(Pageable pageable, Set<String> allowedSorts) {
        if (pageable.getSort().isUnsorted()) {
            return pageable;
        }

        // 获取排序中的无效字段 -- created_at DESC, amount ASC
        Set<String> invalidFields = StreamSupport.stream(pageable.getSort().spliterator(), false)
                .map(Sort.Order::getProperty)
                .filter(field -> !allowedSorts.contains(field))
                .collect(Collectors.toSet());

        if (!invalidFields.isEmpty()) {
            throw new BaseException(
                    ErrorCode.INVALID_REQUEST,
                    "Invalid sort field(s): " + invalidFields
                            + ". Allowed fields are: " + allowedSorts
            );
        }

        return pageable;
    }
}
