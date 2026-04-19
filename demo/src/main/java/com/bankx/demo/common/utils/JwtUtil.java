package com.bankx.demo.common.utils;

import com.bankx.demo.common.constant.SuperConstant;
import com.bankx.demo.security.properties.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;

    public String generateToken(UUID userId, String email, String roles){

        // ttl 是 3600000s
        Date now = new Date();
        Date expire = new Date(now.getTime() + jwtProperties.getTtl());


        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expire)
                .signWith(signingKey())
                .compact();
    }
    // ─── Token parsing ────────────────────────────────────────
    public UUID extractUserId(String token){
        return UUID.fromString(parseClaims(token).getSubject());
    }

    public String extractEmail(String token){
        return parseClaims(token).get("email", String.class);
    }

    public String extractRoles(String token){
        return parseClaims(token).get("roles", String.class);
    }

    // ─── Token extraction from request ───────────────────────────
    public String extractToken(HttpServletRequest request){
        String header = request.getHeader(SuperConstant.AUTHORIZATION_HEADER);
        if(StringUtils.hasText(header) && header.startsWith(SuperConstant.BEARER_PREFIX)){
            return header.substring(SuperConstant.BEARER_PREFIX.length());
        }
        return null;
    }
    // ─── Validation ───────────────────────────────────────────

    /**
     * Returns true only if the token signature is valid and not expired.
     * All exceptions are caught and logged — callers receive a boolean.
     */
    public boolean isValid(String token){
        try{
            parseClaims(token);
            return true;
        } catch(ExpiredJwtException e){
            log.warn("JWT expired: {}", e.getMessage());
        } catch(UnsupportedJwtException e){
            log.warn("JWT unsupported: {}", e.getMessage());
        } catch (MalformedJwtException e){
            log.warn ("JWT malformed: {}", e.getMessage());
        } catch(SecurityException e){
            log.warn("JWT signature invalid: {}", e.getMessage());
        } catch(IllegalArgumentException e){
            log.warn("JWT empty or null: {}", e.getMessage());
        } catch (Exception e){
            log.warn("JWT validation failed: {}", e.getMessage());
        }
        return false;
    }
    // ─── Resolve RequestID ───────────────────────────────────────────
    public static String resolveRequestId(HttpServletRequest request){
        Object attr = request.getAttribute(("requestId"));

        // "unavailable" only if somehow RequestIdFilter didn't run — should never happen
        return attr != null ? attr.toString() : "unavailable";
    }
    // ─── Internal ─────────────────────────────────────────────
    private Claims parseClaims(String token){
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey(){
        // HMAC-SHA256 requires at least 32 bytes; enforce this at startup
        byte[] keyBytes = jwtProperties.getBase64EncodedSecretKey().getBytes(StandardCharsets.UTF_8);
        if(keyBytes.length < 32){
            throw new IllegalStateException("JWT secret must be at least 32 characters long");
        }
        return Keys.hmacShaKeyFor((keyBytes));
    }

}
