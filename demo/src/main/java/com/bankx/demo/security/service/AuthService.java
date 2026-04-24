package com.bankx.demo.security.service;

import com.bankx.demo.security.vo.AuthResponse;
import com.bankx.demo.security.dto.RegisterRequest;
import com.bankx.demo.security.dto.LoginRequest;
import jakarta.validation.Valid;

public interface AuthService {


    AuthResponse register(@Valid RegisterRequest req);

    AuthResponse login(LoginRequest req);

    void logout(String token);

    void sendVerificationCode(String mail);

}
