package com.webank.wedpr.components.user.response;

import com.webank.wedpr.components.user.entity.WedprPermission;
import java.util.List;
import lombok.Data;

@Data
public class WedprPermissionResponse {
    private long total;

    private List<WedprPermission> permissionList;

    public WedprPermissionResponse(long total, List<WedprPermission> permissionList) {
        this.total = total;
        this.permissionList = permissionList;
    }
}
