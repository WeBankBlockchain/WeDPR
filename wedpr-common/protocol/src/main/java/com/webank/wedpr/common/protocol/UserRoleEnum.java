package com.webank.wedpr.common.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UserRoleEnum {

    /** 管理端管理用户 */
    AGENCY_ADMIN("10", "agency_admin"),

    /** 机构用户 */
    ADMIN_ROLE("1", "admin_user"),

    /** 普通用户 */
    ORIGINAL_USER("2", "original_user");

    private String roleId;
    private String roleName;
}
