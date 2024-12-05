package com.webank.wedpr.components.token.auth.model;

import com.webank.wedpr.common.config.WeDPRConfig;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Created by caryliao on 2024/7/26 16:40 */
@Data
@NoArgsConstructor
public class UserJwtConfig {
    // jwt 生成 secret
    private String secret = WeDPRConfig.apply("wedpr.user.jwt.secret", null);
    // jwt 过期时间
    private Long expireTime = WeDPRConfig.apply("wedpr.user.jwt.expireTime", 36000000L);
    // jwt 间隔符号
    private String delimiter = WeDPRConfig.apply("wedpr.user.jwt.delimiter", "|");
    // jwt 缓存长度
    private Integer cacheSize = WeDPRConfig.apply("wedpr.user.jwt.cacheSize", 10000);
    // 私钥sm2
    private String privateKey = WeDPRConfig.apply("wedpr.user.jwt.privateKey", null);
    // the hex public key
    private String hexPublicKey;
    // 对等加密秘钥
    private String sessionKey = WeDPRConfig.apply("wedpr.user.jwt.sessionKey", null);
    // 重试次数
    private Integer maxTryCount = 5;
    // 验证码长度
    private Integer codeLength = 4;
    // 验证码时长s
    private Integer validTime = 3 * 60;
    // 登录错误等待时长ms
    private Long limitTimeThreshold = 10 * 60 * 1000L;

    public UserJwtConfig(String hexPublicKey) {
        setHexPublicKey(hexPublicKey);
    }
}
