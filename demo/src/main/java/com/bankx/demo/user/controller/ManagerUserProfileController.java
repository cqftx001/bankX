package com.bankx.demo.user.controller;

import com.bankx.demo.common.base.ResponseResult;
import com.bankx.demo.common.utils.RequestUtils;
import com.bankx.demo.user.dto.UpdateUserProfileRequest;
import com.bankx.demo.user.service.UserProfileService;
import com.bankx.demo.user.vo.UserProfileVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/manager/profile")
@Tag(name = "Manager User Profile Controller", description = "Manager User Profile API")
public class ManagerUserProfileController {

    private final UserProfileService userProfileService;

    @PutMapping("/{userId}/update")
    @Operation(summary = "Manager Update User Profile - email excluded")
    @PreAuthorize("hasAuthority('USER_PROFILE:UPDATE_ALL')")
    public ResponseEntity<ResponseResult<UserProfileVo>> managerUpdateUserProfile(
            @PathVariable UUID userId,
            @RequestBody UpdateUserProfileRequest req,
            HttpServletRequest request
            ){
        String requestId = RequestUtils.getOrCreateRequestId(request);
        UserProfileVo userProfileVo = userProfileService.updateProfileByManager(userId, req);
        return ResponseEntity.ok(ResponseResult.success(userProfileVo, requestId));
    }

}
