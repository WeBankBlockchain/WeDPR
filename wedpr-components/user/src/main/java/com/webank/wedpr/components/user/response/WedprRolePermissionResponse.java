package com.webank.wedpr.components.user.response;

import com.webank.wedpr.components.user.entity.result.WedprRolePermissionResult;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WedprRolePermissionResponse {
    private long total;
    private List<WedprRolePermissionResult> rolePermissionList;
}
