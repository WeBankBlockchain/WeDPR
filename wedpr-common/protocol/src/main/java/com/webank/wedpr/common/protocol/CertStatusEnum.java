package com.webank.wedpr.common.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CertStatusEnum {
    /** 启用证书 */
    ENABLE_CERT(0, "启用"),

    /** 禁用证书 */
    FORBID_CERT(1, "禁用");

    private Integer statusValue;
    private String statusName;
}
