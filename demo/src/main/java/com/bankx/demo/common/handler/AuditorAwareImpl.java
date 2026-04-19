package com.bankx.demo.common.handler;

import com.bankx.demo.security.model.CustomUserDetails;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Tells Spring JPA Auditing who the "current user" is.
 *
 * Wired into @CreatedBy and @LastModifiedBy on BaseEntity.
 * Returns empty Optional for unauthenticated requests (e.g. /api/auth/login),
 * which leaves createdBy as null — that's intentional for public endpoints.
 */
@Component
public class AuditorAwareImpl implements AuditorAware<UUID> {

    @Override
    public Optional<UUID> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken){
            return Optional.empty();
        }

        // Principal is set by JwtAuthenticationFilter as CustomUserDetails
        // which exposes the user's UUID
        if(authentication.getPrincipal() instanceof CustomUserDetails userDetails){
            return Optional.of(userDetails.getUserId());
        }
        return Optional.empty();
    }
}
