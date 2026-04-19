package com.bankx.demo.security.filter;

import com.bankx.demo.common.utils.JwtUtil;
import com.bankx.demo.security.model.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;


    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = jwtUtil.extractToken(request);

        if(token != null && jwtUtil.isValid(token)){
            try {
                // Build principal and authorities from token claims - no DB calls
                UUID userId = jwtUtil.extractUserId(token);
                String email = jwtUtil.extractEmail(token);
                String roleString = jwtUtil.extractRoles(token);
                // extact authorization
                List<SimpleGrantedAuthority> authorities = parseAuthorities(roleString);

                CustomUserDetails userDetails = new CustomUserDetails(userId, email, null, true,  authorities);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("JWT authenticated: userId={}, email={}, roles={}", userId, email, roleString);
            } catch (Exception e){
                // don't leak exception details in logs, just log that authentication failed
                log.warn("Failed to set authentication from JWT: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    // —— helper ————————————————————————————————————————————————
    private List<SimpleGrantedAuthority> parseAuthorities(String roleString){
        if(!StringUtils.hasText(roleString)) return List.of();

        return Arrays.stream(roleString.split( ","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}
