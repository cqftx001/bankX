package com.bankx.demo.security.properties;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * JWT 配置类，用于从 application.yml 中加载 JWT 相关的配置项
 */
@Data
@Component
@NoArgsConstructor
@ToString
@Configuration
@ConfigurationProperties(prefix = "bankx.framework.jwt")
public class JwtProperties {
    /**
     * 签名密钥
     */
    private String base64EncodedSecretKey;

    /**
     * 有效时间
     */
    private long ttl;

}
