package com.webank.wedpr.components.db.mapper.dataset.dao;

import com.webank.wedpr.components.db.mapper.dataset.common.DatasetConstant;
import com.webank.wedpr.components.uuid.generator.WeDPRUuidGenerator;
import lombok.Data;

@Data
public class DatasetPermission {

    private String id = WeDPRUuidGenerator.generateID();
    private String datasetId;
    private Integer permissionType;
    private String permissionScope;
    private String permissionSubjectId;
    private String authTime;

    public static DatasetPermission createEntity(
            String datasetId,
            int permissionType,
            String permissionScope,
            String permissionSubjectId) {
        return createEntity(
                datasetId,
                permissionType,
                permissionScope,
                permissionSubjectId,
                DatasetConstant.MAX_DATASET_PERMISSION_EXPIRED_TIME);
    }

    public static DatasetPermission createEntity(
            String datasetId,
            int permissionType,
            String permissionScope,
            String permissionSubjectId,
            String authTime) {
        DatasetPermission datasetPermission = new DatasetPermission();
        datasetPermission.setDatasetId(datasetId);
        datasetPermission.setPermissionType(permissionType);
        datasetPermission.setPermissionScope(permissionScope);
        datasetPermission.setPermissionSubjectId(permissionSubjectId);
        datasetPermission.setAuthTime(authTime);
        return datasetPermission;
    }
}
