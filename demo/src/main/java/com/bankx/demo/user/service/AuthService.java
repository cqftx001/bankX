package com.bankx.demo.user.service;

import com.bankx.demo.user.vo.AuthResponse;
import com.bankx.demo.user.dto.RegisterRequest;
import com.bankx.demo.user.dto.LoginRequest;
import jakarta.validation.Valid;

public interface AuthService {


    AuthResponse register(@Valid RegisterRequest req);

    AuthResponse login(LoginRequest req);

    void logout(String token);
}
