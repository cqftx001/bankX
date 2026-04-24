package com.bankx.demo.common.constant;

public class SuperConstant {

    public static final String REQUEST_ID = "requestId";

    public static final String AUTHORIZATION_HEADER = "Authorization";

    public static final String TOKEN_TYPE = "Bearer";

    public static final String BEARER_PREFIX =  "Bearer ";

    // Redis Key prefix
    public static final String REDIS_TOKEN_PREFIX = "token:";
    public static final String REDIS_BLACKLIST_PREFIX = "blacklist:";
    public static final String REDIS_FROZEN_USER_PREFIX = "user:frozen:";

    // Email 验证码
    public static final String REDIS_EMAIL_CODE_PREFIX = "email:code";
    public static final String REDIS_EMAIL_LIMIT_PREFIX = "email:limit";
    public static final String EMAIL_CHANGE_PREFIX = "email:change:";


    public static final long EMAIL_CODE_TTL = 30 * 60L;          // 验证码有效期30分钟
    public static final long EMAIL_LIMIT_TTL = 5 * 60L;         // 发送间隔限制300s



}
