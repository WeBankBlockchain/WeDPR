package com.webank.wedpr.common.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ServerTypeEnum {
    /** 站点端 */
    SITE_END("site_end", "站点端"),

    /** 管理端 */
    ADMIN_END("admin_end", "管理端");

    private String name;
    private String cnName;
}
