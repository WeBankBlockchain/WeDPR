package com.webank.wedpr.components.user.requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * @author zachma
 * @date 2024/7/17
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WedprPermissionRequest {
    private String permissionName;
    private String permissionContent;
}
