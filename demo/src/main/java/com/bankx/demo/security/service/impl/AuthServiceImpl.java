package com.bankx.demo.security.service.impl;

import com.bankx.demo.common.constant.SuperConstant;
import com.bankx.demo.common.enums.ErrorCode;
import com.bankx.demo.common.enums.RoleEnum;
import com.bankx.demo.common.enums.UserStatus;
import com.bankx.demo.common.exception.BaseException;
import com.bankx.demo.common.utils.JwtUtil;
import com.bankx.demo.security.Entity.Role;
import com.bankx.demo.security.Entity.UserRole;
import com.bankx.demo.security.properties.JwtProperties;
import com.bankx.demo.security.dto.LoginRequest;
import com.bankx.demo.security.dto.RegisterRequest;
import com.bankx.demo.user.entity.*;
import com.bankx.demo.security.repository.RoleRepository;
import com.bankx.demo.user.repository.UserRepository;
import com.bankx.demo.security.service.AuthService;
import com.bankx.demo.security.vo.AuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    //--- register ---
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest req) {

        // 1.验证码校验
        String codeKey = SuperConstant.REDIS_EMAIL_CODE_PREFIX + req.getEmail();
        String storedCode = redisTemplate.opsForValue().get(codeKey);

        if(storedCode == null){
            throw new BaseException(ErrorCode.INVALID_REQUEST, "Invalid email verification code");
        }

        if(!storedCode.equals(req.getCode())){
            throw new BaseException(ErrorCode.WRONG_VERIFICATION_CODE, "Invalid email verification code");
        }

        // 验证通过
        redisTemplate.delete(codeKey);

        if(userRepository.existsByEmail(req.getEmail())){
            throw new BaseException(ErrorCode.DUPLICATE_REQUEST, "Email already registered: " + req.getEmail());
        }
        // Build User (Credentials only)
        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));

        user.setStatus(UserStatus.ACTIVE);

        // Build UserProfile (Personal Info)
        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setFirstName(req.getFirstName());
        profile.setLastName(req.getLastName());
        profile.setPhone(req.getPhone());
        profile.setBirthDate(req.getDateOfBirth());
        profile.setAddressLine1(req.getAddressLine1());
        profile.setAddressLine2(req.getAddressLine2());
        profile.setCity(req.getCity());
        profile.setState(req.getState());
        profile.setCountry(req.getCountry());
        profile.setPostalCode(req.getZipCode());

        user.setProfile(profile);

        // Assign default ROLE_CUSTOMER
        Role customerRole = roleRepository.findByName(RoleEnum.ROLE_CUSTOMER)
                .orElseThrow(() -> new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "Default role not found — ensure DataInitializer has run"));
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(customerRole);
        userRole.setAssignedAt(LocalDateTime.now());
        userRole.setAssignedBy(user.getId());
        user.getUserRoles().add(userRole);

        // Save User cascades to UserProfile and UserRole
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), RoleEnum.ROLE_CUSTOMER.name());

        log.info("Token TTL from properties: {}ms", jwtProperties.getTtl());
        // 存入 Redis，key = "token:{userId}", TTL = jwtProperties.getTtl() 毫秒
        redisTemplate.opsForValue().set(
                SuperConstant.REDIS_TOKEN_PREFIX + user.getId(),
                token,
                jwtProperties.getTtl(),
                TimeUnit.MILLISECONDS
        );

        log.info("Customer registered: userId={}, email={}", user.getId(), user.getEmail());

        return new AuthResponse(token,
                SuperConstant.TOKEN_TYPE,
                jwtProperties.getTtl(),
                user.getId(),
                user.getEmail(),
                RoleEnum.ROLE_CUSTOMER.name());
    }

    //--- login ---
    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {

        Authentication authentication;
        try{
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        }catch(BadCredentialsException e){
            throw new BaseException(ErrorCode.UNAUTHORIZED, "Invalid email or password");
        }

        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), roles);


        redisTemplate.opsForValue().set(
                SuperConstant.REDIS_TOKEN_PREFIX + user.getId(),
                token,
                jwtProperties.getTtl(),
                TimeUnit.MILLISECONDS);

        log.info("User logged in: userId={}, email={}", user.getId(), user.getEmail());

        return new AuthResponse(token, SuperConstant.TOKEN_TYPE, jwtProperties.getTtl(), user.getId(), user.getEmail(), roles);
    }

    @Override
    public void logout(String token) {
        if(!jwtUtil.isValid(token)){
            throw new BaseException(ErrorCode.UNAUTHORIZED, "Invalid token");
        }

        UUID userId = jwtUtil.extractUserId(token);
        long remainingTtl = jwtUtil.getRemainingTtl(token);

        // 删除 Redis 中的 token
        redisTemplate.delete(SuperConstant.REDIS_TOKEN_PREFIX + userId);

        // 把token加入黑名单, 设置TTL为剩余的TTL
        if(remainingTtl > 0){
            redisTemplate.opsForValue().set(
                    SuperConstant.REDIS_BLACKLIST_PREFIX + token,
                    "1",
                    remainingTtl,
                    TimeUnit.SECONDS
            );
        }
        log.info("User logged out: userId={}", userId);
    }

    // send code

    @Override
    public void sendVerificationCode(String email) {
        // 1. 限流检查
        String limitKey = SuperConstant.REDIS_EMAIL_LIMIT_PREFIX + email;
        if(Boolean.TRUE.equals(redisTemplate.hasKey(limitKey))){
            throw new BaseException(ErrorCode.TOO_MANY_REQUESTS,
                    "Please wait 5 minutes before requesting another code");
        }

        // 2. 检查邮箱是否已注册
        if(userRepository.existsByEmail(email)){
            throw new BaseException(ErrorCode.DUPLICATE_REQUEST,
                    "Email has already registered: " + email);
        }

        // 3. 生成6位随机验证码
        String code = RandomStringUtils.randomNumeric(6);

        // 4. 存入Redis, valid for 30 mins
        String codeKey = SuperConstant.REDIS_EMAIL_CODE_PREFIX + email;
        redisTemplate.opsForValue().set(
                codeKey,
                code,
                SuperConstant.EMAIL_CODE_TTL,
                TimeUnit.SECONDS
        );

        // 5. 设置限流TTL 5 mins for resend
        redisTemplate.opsForValue().set(
                limitKey,
                "1",
                SuperConstant.EMAIL_LIMIT_TTL,
                TimeUnit.SECONDS
        );

        sendEmail(email, code);

        log.info("Verification code sent: email={}, code={}", email, code);
    }

    // -- helper --
    private void sendEmail(String toEmail, String code){
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(toEmail);
            message.setSubject("BankX - Your Verification Code");
            message.setText(
                    "Hi,\n\n" +
                            "Your BankX verification code is: " + code + "\n\n" +
                            "This code will expire in 30 minutes.\n" +
                            "If you did not request this, please ignore this email.\n\n" +
                            "BankX Team"
            );
            mailSender.send(message);
        } catch(Exception e){
            log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Failed to send verification email, please try again");
        }
    }
}
