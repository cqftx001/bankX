package com.bankx.demo.user.service.impl;

import com.bankx.demo.common.enums.ErrorCode;
import com.bankx.demo.common.enums.UserStatus;
import com.bankx.demo.common.exception.BaseException;
import com.bankx.demo.security.model.CustomUserDetails;
import com.bankx.demo.user.entity.User;
import com.bankx.demo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                isAccountNonLocked(user),
                buildAuthorities(user)
        );
    }

    // —— Helper ————————————————————————————————————————————————————————————————
    private boolean isAccountNonLocked(User user) {
        return user.getStatus() == UserStatus.ACTIVE;
    }


    /**
     * Builds both role authorities (ROLE_CUSTOMER) and
     * fine-grained permission authorities (ACCOUNT:READ_OWN).
     *
     * This dual approach lets us use both:
     *   .hasRole("CUSTOMER")              — coarse
     *   .hasAuthority("ACCOUNT:READ_OWN") — fine-grained
     */
    private Collection<? extends GrantedAuthority> buildAuthorities(User user) {
        // Role-level authorities
        var roleAuthorities = user.getUserRoles().stream()
                .map(userRoles -> new SimpleGrantedAuthority(userRoles.getRole().getName().name()))
                .collect(Collectors.toSet());

        // Permission-level authorities from all assigned roles
        // e.g. ROLE_CUSTOMER → [ACCOUNT_READ, TRANSFER_CREATE]
        //     ROLE_ADMIN    → [USER_MANAGE, LOAN_APPROVE]    -> [ ACCOUNT_READ, TRANSFER_CREATE, USER_MANAGE, LOAN_APPROVE ]
        var permissionAuthorities = user.getUserRoles().stream()
                .flatMap(userRoles -> userRoles.getRole().getRolePermissions().stream())
                .map(rolePermissions -> new SimpleGrantedAuthority(
                        rolePermissions.getPermission().getResource() + ":" + rolePermissions.getPermission().getAction()))
                .collect(Collectors.toSet());

        roleAuthorities.addAll(permissionAuthorities);
        return roleAuthorities;
    }
}
