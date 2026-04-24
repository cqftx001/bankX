package com.bankx.demo.security.controller;

import com.bankx.demo.common.base.ResponseResult;
import com.bankx.demo.common.enums.ErrorCode;
import com.bankx.demo.common.exception.BaseException;
import com.bankx.demo.common.utils.JwtUtil;
import com.bankx.demo.common.utils.RequestUtils;
import com.bankx.demo.security.vo.AuthResponse;
import com.bankx.demo.security.dto.LoginRequest;
import com.bankx.demo.security.dto.RegisterRequest;
import com.bankx.demo.security.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register and login endpoints - no token needed")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    /**
     * POST /api/v1/auth/register
     *
     * Creates a new customer account and returns a JWT.
     * The user is immediately authenticated after registration (BOA UX pattern).
     * No existing session required.
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new customer account")
    public ResponseEntity<ResponseResult<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest req,
            HttpServletRequest request){

        String requestId = RequestUtils.getOrCreateRequestId(request);
        AuthResponse authResponse = authService.register(req);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseResult.success(authResponse, requestId));

    }

    /**
     * POST /api/v1/auth/login
     *
     * Authenticates an existing customer and returns a JWT.
     * The token must be included in subsequent requests as:
     *   Authorization: Bearer <token>
     */
    @PostMapping("/login")
    @Operation(summary = "Login an existing customer")
    public ResponseEntity<ResponseResult<AuthResponse>> login(
        @Valid @RequestBody LoginRequest req,
        HttpServletRequest request){

        String requestId = RequestUtils.getOrCreateRequestId(request);
        AuthResponse authResponse = authService.login(req);

        return ResponseEntity.ok(ResponseResult.success(authResponse, requestId));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and invalidate token")
    public ResponseEntity<ResponseResult<Void>> logout(
            HttpServletRequest request) {

        String requestId = RequestUtils.getOrCreateRequestId(request);
        String token = jwtUtil.extractToken(request);

        if (token == null) {
            throw new BaseException(ErrorCode.UNAUTHORIZED, "No token provided");
        }

        authService.logout(token);
        return ResponseEntity.ok(ResponseResult.success(requestId));
    }

    @PostMapping("/send-code")
    @Operation(summary = "Send verification code to email")
    public ResponseEntity<ResponseResult<Void>> sendVerification(
        @RequestParam @Email(message = "Invalid email format") String email,
        HttpServletRequest request){

        String requestId = RequestUtils.getOrCreateRequestId(request);
        authService.sendVerificationCode(email);

        return ResponseEntity.ok(ResponseResult.success(requestId));
    }

}
