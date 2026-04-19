package com.bankx.demo.security.model;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

/**
 * Spring Security principal for an authenticated user.
 *
 * Extends the standard UserDetails contract so Spring Security can use it,
 * while also exposing userId (UUID) which is needed by AuditorAwareImpl
 * to populate @CreatedBy / @LastModifiedBy on BaseEntity.
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final UUID   userId;
    private final String email;
    private final String password;
    private final boolean accountNonLocked;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(UUID userId,
                             String email,
                             String password,
                             boolean accountNonLocked,
                             Collection<? extends GrantedAuthority> authorities) {
        this.userId          = userId;
        this.email           = email;
        this.password        = password;
        this.accountNonLocked = accountNonLocked;
        this.authorities     = authorities;
    }
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired(){
        return true;
    }

    @Override
    public boolean isAccountNonLocked(){
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired(){
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
