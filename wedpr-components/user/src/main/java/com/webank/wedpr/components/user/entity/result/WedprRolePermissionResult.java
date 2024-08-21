package com.webank.wedpr.components.user.entity.result;

import lombok.Data;

@Data
public class WedprRolePermissionResult {
    private String roleId;
    private String roleName;
    private String permissionId;
    private String permissionName;
    private String permissionContent;
}
