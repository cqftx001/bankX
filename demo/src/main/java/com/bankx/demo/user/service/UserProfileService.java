package com.bankx.demo.user.service;

import com.bankx.demo.security.vo.AuthResponse;
import com.bankx.demo.user.UserProfileVo;
import com.bankx.demo.user.dto.UpdateProfileRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;

import java.util.UUID;

public interface UserProfileService {

    UserProfileVo getMyProfile(UUID userId);

    UserProfileVo updateMyProfile(UUID userId, @Valid UpdateProfileRequest req);

    void requestEmailChange(UUID userId, @Email String newEmail);

    AuthResponse confirmEmailChange(UUID userId, String code, HttpServletRequest request);
}
