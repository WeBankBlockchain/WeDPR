package com.webank.wedpr.components.user.requests;

import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author zachma
 * @date 2024/7/17
 */
@Data
public class WedprRolePermissionRequest {
    @NotBlank(message = "角色名不能为空")
    private String roleName;

    @NotBlank(message = "权限Id不能为空")
    private String permissionId;
}
