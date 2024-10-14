package com.webank.wedpr.common.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CertStatusViewEnum {

    /** 无证书 */
    NO_CERT(0, "无证书"),

    /** 有效证书 */
    VALID_CERT(1, "有效"),

    /** 过期证书 */
    EXPIRED_CERT(2, "过期"),

    /** 禁用证书 */
    FORBID_CERT(3, "禁用");

    private Integer statusValue;
    private String statusName;
}
