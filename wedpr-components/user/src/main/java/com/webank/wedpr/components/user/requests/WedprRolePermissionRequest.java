package com.webank.wedpr.components.user.requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author zachma
 * @date 2024/7/17
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WedprRolePermissionRequest {
    @NotBlank(message = "角色名不能为空")
    private String roleName;

    @NotBlank(message = "权限Id不能为空")
    private String permissionId;
}
