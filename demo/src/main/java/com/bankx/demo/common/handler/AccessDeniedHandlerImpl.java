package com.bankx.demo.common.handler;

import com.bankx.demo.common.enums.ErrorCode;
import com.bankx.demo.common.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Called when an authenticated user tries to access a resource
 * they don't have permission for (e.g. CUSTOMER hitting /api/admin/**).
 *
 * Returns HTTP 403 with a consistent JSON body instead of Spring's
 * default HTML error page.
 */
@Slf4j
@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        String requestId = JwtUtils.resolveRequestId(request);

        log.warn("Access denied. requestId={}, principal={}, method={}, uri={}",
                requestId,
                request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "unknown",
                request.getMethod(),
                request.getRequestURI());

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", ErrorCode.FORBIDDEN.getCode());
        body.put("message", ErrorCode.FORBIDDEN.getMessage());
        body.put("data", null);
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("requestId", requestId);

        MAPPER.writeValue(response.getOutputStream(), body);
    }
}
