package com.webank.wedpr.components.dataset.service;

import com.webank.wedpr.common.utils.Common;
import com.webank.wedpr.components.db.mapper.dataset.common.DatasetConstant;
import com.webank.wedpr.components.db.mapper.dataset.dao.DatasetAuthContent;
import com.webank.wedpr.components.db.mapper.dataset.dao.DatasetUserPermissions;
import com.webank.wedpr.components.db.mapper.dataset.dao.UserInfo;
import com.webank.wedpr.components.db.mapper.dataset.exception.DatasetException;
import com.webank.wedpr.components.db.mapper.dataset.mapper.DatasetPermissionMapper;
import com.webank.wedpr.components.db.mapper.dataset.mapper.wapper.DatasetTransactionalWrapper;
import com.webank.wedpr.components.db.mapper.dataset.permission.DatasetUserPermissionValidator;
import com.webank.wedpr.components.token.auth.model.GroupInfo;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("datasetAuth")
@Data
public class DatasetAuthImpl implements DatasetAuthApi {
    private static final Logger logger = LoggerFactory.getLogger(DatasetAuthImpl.class);

    @Autowired private DatasetTransactionalWrapper datasetTransactionalWrapper;

    @Autowired private DatasetPermissionMapper datasetPermissionMapper;

    @Override
    public void authorizeDatasetPermission(
            String id,
            String datasetId,
            int permissionType,
            String authTime,
            UserInfo authorizedUser)
            throws DatasetException {

        long startTimeMillis = System.currentTimeMillis();

        logger.info(
                "authorize dataset permission begin, id: {}, datasetId: {}, permissionType: {}, authTime: {}, authorizedUser: {}",
                id,
                datasetId,
                permissionType,
                authTime,
                authorizedUser);
        try {
            datasetTransactionalWrapper.transactionalAuthDatasetPermission(
                    datasetId, permissionType, authorizedUser, authTime);

            logger.info("authorize dataset permission success, id: {}", id);

        } catch (Exception e) {
            logger.error("authorize dataset failed, datasetId: {}, e: ", datasetId, e);
            throw new DatasetException("authorize dataset permission failed, e: " + e.getMessage());
        }

        long endTimeMillis = System.currentTimeMillis();
        logger.info(
                "authorize dataset permission end, id: {}, datasetId: {}, permissionType: {}, authTime: {}, authorizedUser: {}, cost(ms): {}",
                id,
                datasetId,
                permissionType,
                authTime,
                authorizedUser,
                (endTimeMillis - startTimeMillis));
    }

    @Override
    public void authorizeDatasetPermissionList(
            UserInfo userInfo, List<DatasetAuthContent> datasetAuthContentList)
            throws DatasetException {

        long startTimeMillis = System.currentTimeMillis();

        logger.info(
                "authorize dataset permission list begin, datasetAuthContentList: {}",
                datasetAuthContentList);
        try {

            for (DatasetAuthContent datasetAuthContent : datasetAuthContentList) {
                String datasetId = datasetAuthContent.getDatasetId();
                Common.requireNonEmpty("datasetId", datasetId);
                String authTime = datasetAuthContent.getAuthTime();
                Common.requireNonEmpty("authTime", authTime);
                Common.isValidDateFormat(authTime);

                Integer permissionType = datasetAuthContent.getPermissionType();
                if (permissionType == null) {
                    datasetAuthContent.setPermissionType(
                            DatasetConstant.DatasetPermissionType.USABLE.getType());
                } else {
                    DatasetConstant.DatasetPermissionType.isValidDatasetPermissionType(
                            permissionType);
                }
            }

            datasetTransactionalWrapper.transactionalAuthDatasetPermissionList(
                    userInfo, datasetAuthContentList);

            logger.info("authorize dataset permission list success");

        } catch (DatasetException datasetException) {
            throw datasetException;
        } catch (Exception e) {
            logger.error("authorize dataset permission list failed, e: ", e);
            throw new DatasetException(
                    "authorize dataset permission list failed, e: " + e.getMessage());
        }

        long endTimeMillis = System.currentTimeMillis();
        logger.info(
                "authorize dataset permission list end, cost(ms): {}",
                (endTimeMillis - startTimeMillis));
    }

    @Override
    public void revokeDatasetPermission(
            String id, String datasetId, int permissionType, UserInfo authorizedUser)
            throws DatasetException {

        logger.info(
                "revoke dataset permission begin, id: {}, datasetId: {}, permissionType: {}, authorizedUser: {}",
                id,
                datasetId,
                permissionType,
                authorizedUser);

        try {

            datasetTransactionalWrapper.transactionalRevokeAuthDatasetPermission(
                    datasetId, permissionType, authorizedUser);

            logger.info("revoke dataset permission success, datasetId: {}", datasetId);
        } catch (Exception e) {
            logger.error("revoke dataset permission failed, datasetId: {}, e: ", datasetId, e);
            throw new DatasetException("revoke dataset permission failed, e: " + e.getMessage());
        }
    }

    @Override
    public DatasetUserPermissions queryUserPermissions(
            String datasetId, String user, String userGroup, String agency)
            throws DatasetException {

        logger.info(
                "query dataset permission begin, datasetId: {}, user: {}, userGroup: {}, agency: {}",
                datasetId,
                user,
                userGroup,
                agency);

        List<GroupInfo> groupInfoList = new ArrayList<>();
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setGroupName(userGroup);
        groupInfo.setGroupId(userGroup);
        groupInfoList.add(groupInfo);

        UserInfo userInfo =
                UserInfo.builder().user(user).groupInfos(groupInfoList).agency(agency).build();

        DatasetUserPermissions datasetUserPermissions =
                DatasetUserPermissionValidator.confirmUserDatasetPermissions(
                        datasetId, userInfo, datasetPermissionMapper, false);

        logger.info(
                "query dataset permission success, datasetId: {}, user: {}, userGroup: {}, agency: {}, datasetUserPermissions: {}",
                datasetId,
                user,
                userGroup,
                agency,
                datasetUserPermissions);
        return datasetUserPermissions;
    }
}
