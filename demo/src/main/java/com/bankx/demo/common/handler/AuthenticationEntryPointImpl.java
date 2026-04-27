package com.bankx.demo.common.handler;

import com.bankx.demo.common.enums.ErrorCode;
import com.bankx.demo.common.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Called by Spring Security when a request hits a protected endpoint without a valid token (i.e. authentication is missing or invalid).
 *
 * Default Spring behavior returns an HTML error page — useless for a REST API.
 * This handler returns the same JSON shape as ResponseResult so the client
 * always gets a consistent response regardless of where the error originated.
 */
@Slf4j
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        String requestId = JwtUtils.resolveRequestId(request);

        log.warn("Unauthenticated access. requestId={}, method={}, uri={}",
                requestId, request.getMethod(), request.getRequestURI());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Mirror ResponseResult's exact JSON shape so clients see
        // a consistent envelope regardless of where the error originates

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", ErrorCode.UNAUTHORIZED.getCode());
        body.put("message", ErrorCode.UNAUTHORIZED.getMessage());
        body.put("data", null);
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("requestId", requestId);

        MAPPER.writeValue(response.getOutputStream(), body);
    }
}
