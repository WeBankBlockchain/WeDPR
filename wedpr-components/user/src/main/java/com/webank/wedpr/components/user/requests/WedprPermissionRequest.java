package com.webank.wedpr.components.user.requests;

import lombok.Data;

/**
 * @author zachma
 * @date 2024/7/17
 */
@Data
public class WedprPermissionRequest {
    private String permissionName;
    private String permissionContent;
}
