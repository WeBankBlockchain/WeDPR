package com.webank.wedpr.core.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UserRoleEnum {
    /** 机构用户 */
    ADMIN_ROLE("1", "admin_user"),

    /** 普通用户 */
    ORIGINAL_USER("2", "original_user");

    private String roleId;
    private String roleName;
}
