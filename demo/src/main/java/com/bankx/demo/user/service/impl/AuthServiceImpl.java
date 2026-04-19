package com.bankx.demo.user.service.impl;

import com.bankx.demo.common.constant.SuperConstant;
import com.bankx.demo.common.enums.ErrorCode;
import com.bankx.demo.common.enums.UserStatus;
import com.bankx.demo.common.exception.BaseException;
import com.bankx.demo.common.utils.JwtUtil;
import com.bankx.demo.security.properties.JwtProperties;
import com.bankx.demo.user.entity.*;
import com.bankx.demo.user.repository.RoleRepository;
import com.bankx.demo.user.repository.UserRepository;
import com.bankx.demo.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    //--- register ---
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest req) {

        if(userRepository.existsByEmail(req.getEmail())){
            throw new BaseException(ErrorCode.DUPLICATE_REQUEST, "Email already registered: " + req.getEmail());
        }
        // Build User (Credentials only)
        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
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
        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseThrow(() -> new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "Default role not found — ensure DataInitializer has run"));
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(customerRole);
        userRole.setAssignedAt(LocalDateTime.now());
        userRole.setAssignedBy(user.getId());
        user.getUserRoles().add(userRole);

        // Save User cascades to UserProfile and UserRole
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), SuperConstant.DEFAULT_ROLE);

        log.info("Customer registered: userId={}, email={}", user.getId(), user.getEmail());

        return new AuthResponse(token, SuperConstant.TOKEN_TYPE, jwtProperties.getTtl(), user.getId(), user.getEmail(), SuperConstant.DEFAULT_ROLE);
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

        log.info("User logged in: userId={}, email={}", user.getId(), user.getEmail());

        return new AuthResponse(token, SuperConstant.TOKEN_TYPE, jwtProperties.getTtl(), user.getId(), user.getEmail(), roles);

    }
}
