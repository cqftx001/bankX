package com.bankx.demo.user.controller;

import com.bankx.demo.common.base.ResponseResult;
import com.bankx.demo.common.utils.RequestUtils;
import com.bankx.demo.security.model.CustomUserDetails;
import com.bankx.demo.security.vo.AuthResponse;
import com.bankx.demo.user.UserProfileVo;
import com.bankx.demo.user.dto.UpdateProfileRequest;
import com.bankx.demo.user.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "User Profile API")
public class UserProfileController {

    private UserProfileService userProfileService;


    @GetMapping
    @Operation(summary = "Get User Profile")
    @PreAuthorize("hasAuthority('USER_PROFILE:READ_OWN')")
    public ResponseEntity<ResponseResult<UserProfileVo>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
            ){

        String requestId = RequestUtils.getOrCreateRequestId(request);
        UserProfileVo userProfileVo = userProfileService.getMyProfile(userDetails.getUserId());
        return ResponseEntity.ok(ResponseResult.success(userProfileVo, requestId));
    }

    @PutMapping("/update")
    @Operation(summary = "Update User Profile - email excluded")
    @PreAuthorize("hasAuthority('USER_PROFILE:UPDATE')")
    public ResponseEntity<ResponseResult<UserProfileVo>> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest req,
            HttpServletRequest request
            ){

        String requestId = RequestUtils.getOrCreateRequestId(request);
        UserProfileVo userProfileVo = userProfileService.updateMyProfile(userDetails.getUserId(), req);
        return ResponseEntity.ok(ResponseResult.success(userProfileVo, requestId));
    }
    @PostMapping("/email/request")
    @Operation(summary = "Request Email Verification")
    @PreAuthorize("hasAuthority('USER_PROFILE:UPDATE')")
    public ResponseEntity<ResponseResult<Void>> requestEmailChange(
            @RequestParam @Email(message = "Invalid email form") String newEmail,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ){

        String requestId = RequestUtils.getOrCreateRequestId(request);
        userProfileService.requestEmailChange(userDetails.getUserId(), newEmail);
        return ResponseEntity.ok(ResponseResult.success(requestId));
    }

    @PutMapping("/email/confirm")
    @Operation(summary = "Confirm Email Verification")
    @PreAuthorize("hasAuthority('USER_PROFILE:UPDATE')")
    public ResponseEntity<ResponseResult<AuthResponse>> confirmEmailChange(
            @RequestParam String code,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ){
        String requestId = RequestUtils.getOrCreateRequestId(request);
        AuthResponse response = userProfileService.confirmEmailChange(
                userDetails.getUserId(), code, request
        );

        return ResponseEntity.ok()
                .body(ResponseResult.success(response, requestId));
    }

}
