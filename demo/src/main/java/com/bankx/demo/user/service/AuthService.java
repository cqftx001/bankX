package com.bankx.demo.user.service;

import com.bankx.demo.user.entity.AuthResponse;
import com.bankx.demo.user.entity.RegisterRequest;
import com.bankx.demo.user.entity.LoginRequest;
import jakarta.validation.Valid;

public interface AuthService {


    AuthResponse register(@Valid RegisterRequest req);

    AuthResponse login(LoginRequest req);
}
