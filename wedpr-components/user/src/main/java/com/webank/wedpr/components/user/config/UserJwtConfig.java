package com.webank.wedpr.components.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Created by caryliao on 2024/7/26 16:40 */
@Component
@ConfigurationProperties(prefix = "wedpr.user.jwt")
@Data
public class UserJwtConfig {
    // jwt 生成 secret
    private String secret;
    // jwt 过期时间
    private Long expireTime;
    // jwt 间隔符号
    private String delimiter;
    // jwt 缓存长度
    private Integer cacheSize;
    // 私钥sm2
    private String privateKey;
    // 公钥sm2
    private String publicKey;
    // 对等加密秘钥
    private String sessionKey;
    // 重试次数
    private Integer maxTryCount = 5;
    // 验证码长度
    private Integer codeLength = 4;
    // 验证码时长s
    private Integer validTime = 3 * 60;
    // 登录错误等待时长ms
    private Long limitTimeThreshold = 10 * 60 * 1000L;
}
