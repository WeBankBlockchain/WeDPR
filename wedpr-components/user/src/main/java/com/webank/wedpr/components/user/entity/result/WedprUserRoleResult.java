package com.webank.wedpr.components.user.entity.result;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WedprUserRoleResult {
    private String username;
    private String roleId;
    private String roleName;
}
