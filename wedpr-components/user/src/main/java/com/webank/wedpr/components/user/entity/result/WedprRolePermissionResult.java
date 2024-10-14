package com.webank.wedpr.components.user.entity.result;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WedprRolePermissionResult {
    private String roleId;
    private String roleName;
    private String permissionId;
    private String permissionName;
    private String permissionContent;
}
