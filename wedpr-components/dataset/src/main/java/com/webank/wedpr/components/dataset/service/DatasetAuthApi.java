package com.webank.wedpr.components.dataset.service;

import com.webank.wedpr.components.db.mapper.dataset.dao.DatasetAuthContent;
import com.webank.wedpr.components.db.mapper.dataset.dao.DatasetUserPermissions;
import com.webank.wedpr.components.db.mapper.dataset.dao.UserInfo;
import com.webank.wedpr.components.db.mapper.dataset.exception.DatasetException;
import java.util.List;

public interface DatasetAuthApi {
    void authorizeDatasetPermission(
            String id,
            String datasetId,
            int permissionType,
            String authTime,
            UserInfo authorizedUser)
            throws DatasetException;

    void authorizeDatasetPermissionList(
            UserInfo authorizedUser, List<DatasetAuthContent> datasetAuthContentList)
            throws DatasetException;

    void revokeDatasetPermission(
            String id, String datasetId, int permissionType, UserInfo authorizedUser)
            throws DatasetException;

    DatasetUserPermissions queryUserPermissions(
            String datasetId, String user, String userGroup, String agency) throws DatasetException;
}
