package com.bankx.demo.common.utils;

import com.bankx.demo.common.constant.SuperConstant;
import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

/**
 * Utility class for handling HTTP requests
 */
public final class RequestUtils {

    private RequestUtils() {
    }

    /**
     * Read requestId from request attributes.
     * If not found, generate one as a fallback.
     *
     * 中文：
     * 优先从请求上下文中获取 requestId；
     * 如果当前请求还没有注入 requestId，则自动生成一个兜底值。
     *
     * @param request current HTTP request
     * @return request trace ID
     */
    public static String getOrCreateRequestId(HttpServletRequest request){
        Object requestIdAttr = request.getAttribute(SuperConstant.REQUEST_ID);
        return requestIdAttr != null ? requestIdAttr.toString() : UUID.randomUUID().toString();
    }
}


