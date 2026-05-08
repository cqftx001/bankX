package com.bankx.demo.user.service;

import com.bankx.demo.security.vo.AuthResponse;
import com.bankx.demo.user.dto.UpdateUserProfileRequest;
import com.bankx.demo.user.vo.UserProfileVo;
import com.bankx.demo.user.dto.UpdateMyProfileRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;

import java.util.UUID;

public interface UserProfileService {

    UserProfileVo getMyProfile(UUID userId);

    UserProfileVo updateMyProfile(UUID userId, @Valid UpdateMyProfileRequest req);

    UserProfileVo updateProfileByManager(UUID userId, @Valid UpdateUserProfileRequest req);

    void requestEmailChange(UUID userId, @Email String newEmail);

    AuthResponse confirmEmailChange(UUID userId, String code, HttpServletRequest request);
}
