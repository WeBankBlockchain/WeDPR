package com.webank.wedpr.components.db.mapper.dataset.dao;

import com.webank.wedpr.components.db.mapper.dataset.common.DatasetConstant.DatasetPermissionType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DatasetUserPermissions {
    private boolean readable;
    private boolean visible;
    private boolean usable;
    private boolean writable;

    public boolean hasPermission(int permissionType) {
        return ((permissionType == DatasetPermissionType.VISIBLE.getType()) && visible)
                || ((permissionType == DatasetPermissionType.USABLE.getType()) && usable)
                || ((permissionType == DatasetPermissionType.WRITABLE.getType()) && writable)
                || ((permissionType == DatasetPermissionType.READABLE.getType()) && readable);
    }

    public boolean isReadable() {
        return hasPermission(DatasetPermissionType.READABLE.getType());
    }

    public boolean isVisible() {
        return hasPermission(DatasetPermissionType.VISIBLE.getType());
    }

    public boolean isUsable() {
        return hasPermission(DatasetPermissionType.USABLE.getType());
    }

    public boolean isWritable() {
        return hasPermission(DatasetPermissionType.WRITABLE.getType());
    }
}
