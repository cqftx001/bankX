package com.bankx.demo.user.service.impl;

import com.bankx.demo.common.constant.SuperConstant;
import com.bankx.demo.common.enums.ErrorCode;
import com.bankx.demo.common.exception.BaseException;
import com.bankx.demo.common.utils.JwtUtil;
import com.bankx.demo.security.properties.JwtProperties;
import com.bankx.demo.security.vo.AuthResponse;
import com.bankx.demo.user.UserProfileVo;
import com.bankx.demo.user.dto.UpdateProfileRequest;
import com.bankx.demo.user.entity.User;
import com.bankx.demo.user.entity.UserProfile;
import com.bankx.demo.user.repository.UserRepository;
import com.bankx.demo.user.service.UserProfileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String sender;


    @Override
    @Transactional(readOnly = true)
    public UserProfileVo getMyProfile(UUID userId) {
        return toVO(findUserById(userId));
    }

    @Override
    @Transactional
    public UserProfileVo updateMyProfile(UUID userId, UpdateProfileRequest request) {
        User user = findUserById(userId);
        UserProfile profile = user.getProfile();

        if(profile == null){
            throw new BaseException(ErrorCode.RESOURCE_NOT_FOUND,
                    "Profile not found!");
        }

        if (request.getFirstName() != null) profile.setFirstName(request.getFirstName());
        if (request.getLastName() != null) profile.setLastName(request.getLastName());
        if (request.getPhone() != null) profile.setPhone(request.getPhone());
        if (request.getBirthDate() != null) profile.setBirthDate(request.getBirthDate());
        if (request.getAddressLine1() != null) profile.setAddressLine1(request.getAddressLine1());
        if (request.getAddressLine2() != null) profile.setAddressLine2(request.getAddressLine2());
        if (request.getCity() != null) profile.setCity(request.getCity());
        if (request.getState() != null) profile.setState(request.getState());
        if (request.getZipCode() != null) profile.setPostalCode(request.getZipCode());
        if (request.getCountry() != null) profile.setCountry(request.getCountry());

        userRepository.save(user);
        log.info("Profile updated: userId={}", userId);
        return toVO(user);
    }

    @Override
    public void requestEmailChange(UUID userId, String newEmail) {

        if(userRepository.existsByEmail(newEmail)){
            throw new BaseException(ErrorCode.DUPLICATE_REQUEST,
                    "Email already registered: " + newEmail);
        }

        // 限流检查
        String limitKey = SuperConstant.REDIS_EMAIL_LIMIT_PREFIX + newEmail;
        if(Boolean.TRUE.equals(redisTemplate.hasKey(limitKey))){
            throw new BaseException(ErrorCode.INVALID_REQUEST,
                    "Please wait 300 seconds before requesting another code");
        }

        // 生成验证码
        String code = RandomStringUtils.randomNumeric(6);

        // 存储Redis: 同时保存new email 和 code
        String changeKey = SuperConstant.EMAIL_CHANGE_PREFIX + userId;
        Map<String, String> map = new HashMap<>();
        map.put("email", newEmail);
        map.put("code", code);

        redisTemplate.opsForValue().set(
                changeKey,
                new ObjectMapper().writeValueAsString(map),
                SuperConstant.EMAIL_CODE_TTL,
                TimeUnit.SECONDS
        );

        redisTemplate.opsForValue().set(
                limitKey, "1", SuperConstant.EMAIL_LIMIT_TTL, TimeUnit.SECONDS
        );

        // 发送验证码
        sendEmailChangeCode(newEmail, code);
        log.info("Email change code sent: userId={}, email={}", userId, newEmail);
    }

    @Override
    public AuthResponse confirmEmailChange(UUID userId, String code, HttpServletRequest request) {

        String changedKey = SuperConstant.EMAIL_CHANGE_PREFIX + userId;
        String json = redisTemplate.opsForValue().get(changedKey);
        if(json == null){
            throw new BaseException(ErrorCode.INVALID_REQUEST,
                    "Verification code expired, please request a new one");
        }

        // 解析redis里的数据
        Map<String, String> map = new HashMap<>();
        try{
            map = new ObjectMapper().readValue(json, Map.class);
        }catch (Exception e){
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Failed to parse verification data");
        }

        String storedCode = map.get("code");
        String newEmail = map.get("email");

        if(!storedCode.equals(code)){
            throw new BaseException(ErrorCode.INVALID_REQUEST,
                    "Invalid verification code");
        }

        // 验证通过
        User user = findUserById(userId);
        user.setEmail(newEmail);
        userRepository.save(user);

        // 删除Redis验证码 + 限流
        String limitKey = SuperConstant.REDIS_EMAIL_LIMIT_PREFIX + newEmail;
        redisTemplate.delete(changedKey);

        // 旧token放入黑名单
        String oldToken = jwtUtil.extractToken(request);
        if(oldToken != null){
            long remainingTtl = jwtUtil.getRemainingTtl(oldToken);
            if(remainingTtl > 0){
                redisTemplate.opsForValue().set(
                        SuperConstant.REDIS_BLACKLIST_PREFIX + oldToken,
                        "1",
                        remainingTtl,
                        TimeUnit.SECONDS
                );
            }
        }
        redisTemplate.delete(SuperConstant.REDIS_TOKEN_PREFIX + userId);

        // 生成新的token
        String roles = user.getUserRoles().stream()
                .map(ur -> ur.getRole().getName().name())
                .collect(Collectors.joining(","));

        String newToken = jwtUtil.generateToken(userId, newEmail, roles);

        // update redis
        redisTemplate.opsForValue().set(
                SuperConstant.REDIS_TOKEN_PREFIX + userId,
                newToken,
                jwtProperties.getTtl(),
                TimeUnit.MILLISECONDS
        );

        log.info("Email changed: userId={}, newEmail={}", userId, newEmail);
        return new AuthResponse(newToken,
                SuperConstant.TOKEN_TYPE,
                jwtProperties.getTtl(),
                userId,
                newEmail,
                roles);

    }

    // -- helper --
    private User findUserById(UUID userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        return user;
    }

    private void sendEmailChangeCode(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(sender);
            message.setTo(toEmail);
            message.setSubject("BankX — Email Change Verification");
            message.setText(
                    "Hi,\n\n" +
                            "Your email change verification code is: " + code + "\n\n" +
                            "This code will expire in 5 minutes.\n" +
                            "If you did not request this, please ignore this email.\n\n" +
                            "BankX Team"
            );
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send email change code: {}", e.getMessage());
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Failed to send verification email");
        }
    }

    private UserProfileVo toVO(User user) {
        UserProfile profile = user.getProfile();
        return UserProfileVo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(profile != null ? profile.getFirstName() : null)
                .lastName(profile != null ? profile.getLastName() : null)
                .phone(profile != null ? profile.getPhone() : null)
                .birthDate(profile != null ? profile.getBirthDate() : null)
                .addressLine1(profile != null ? profile.getAddressLine1() : null)
                .addressLine2(profile != null ? profile.getAddressLine2() : null)
                .city(profile != null ? profile.getCity() : null)
                .state(profile != null ? profile.getState() : null)
                .zipCode(profile != null ? profile.getPostalCode() : null)
                .country(profile != null ? profile.getCountry() : null)
                .build();
    }

}
