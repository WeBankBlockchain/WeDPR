package com.webank.wedpr.common.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Created by caryliao on 2024/8/24 21:00 */
@AllArgsConstructor
@Getter
public enum CertScriptCmdEnum {
    CSR_TO_CRT("sign_agency_cert");

    private String name;
}
