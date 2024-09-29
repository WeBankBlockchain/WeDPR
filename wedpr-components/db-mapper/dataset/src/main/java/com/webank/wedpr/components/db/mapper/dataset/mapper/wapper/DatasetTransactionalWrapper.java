package com.webank.wedpr.components.db.mapper.dataset.mapper.wapper;

import com.webank.wedpr.components.db.mapper.dataset.common.DatasetCode;
import com.webank.wedpr.components.db.mapper.dataset.common.DatasetConstant;
import com.webank.wedpr.components.db.mapper.dataset.dao.*;
import com.webank.wedpr.components.db.mapper.dataset.exception.DatasetException;
import com.webank.wedpr.components.db.mapper.dataset.mapper.DatasetMapper;
import com.webank.wedpr.components.db.mapper.dataset.mapper.DatasetPermissionMapper;
import com.webank.wedpr.components.db.mapper.dataset.permission.DatasetPermissionUtils;
import com.webank.wedpr.components.db.mapper.dataset.permission.DatasetUserPermissionValidator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Data
public class DatasetTransactionalWrapper {

    private static final Logger logger = LoggerFactory.getLogger(DatasetTransactionalWrapper.class);

    @Autowired private DatasetMapper datasetMapper;
    @Autowired private DatasetPermissionMapper datasetPermissionMapper;

    @Transactional(rollbackFor = Exception.class)
    public void transactionalAddDataset(
            String datasetId, Dataset dataset, List<DatasetPermission> datasetPermissionList)
            throws DatasetException {

        try {
            int insertDatasetCount = datasetMapper.insertDataset(dataset);
            int insertPermissionCount = 0;
            if (!datasetPermissionList.isEmpty()) {
                insertPermissionCount =
                        datasetPermissionMapper.insertDatasetPermissionList(datasetPermissionList);
            }

            logger.info(
                    "transactional add dataset success, datasetId: {}, dataset: {}, insertDatasetCount: {}, insertPermissionCount :{}",
                    datasetId,
                    dataset,
                    insertDatasetCount,
                    insertPermissionCount);

        } catch (Exception e) {
            logger.error("transactional add dataset exception, datasetId: {}, e: ", datasetId, e);
            throw new DatasetException(
                    DatasetCode.DB_ERROR.getCode(), DatasetCode.DB_ERROR.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Dataset> transactionalDeleteDatasetList(
            UserInfo userInfo, List<String> datasetIdList) throws DatasetException {

        List<Dataset> datasetList = new ArrayList<>();
        if (datasetIdList == null || datasetIdList.isEmpty()) {
            return datasetList;
        }

        String user = userInfo.getUser();
        String agency = userInfo.getAgency();

        // check dataset existence
        for (String datasetId : datasetIdList) {
            Dataset oldDataset = datasetMapper.getDatasetByDatasetId(datasetId, true);
            if (oldDataset == null) {
                logger.error("the dataset does not exist, datasetId: {}", datasetId);
                throw new DatasetException("the dataset does not exist, datasetId: " + datasetId);
            }
            datasetList.add(oldDataset);

            logger.info(" => datasetId: {}, dataset: {}", datasetId, oldDataset);
            // query dataset permissions info
            List<DatasetPermission> oldDatasetPermissionList =
                    datasetPermissionMapper.queryPermissionListForDataset(
                            datasetId, user, agency, true);

            // permission verification
            DatasetUserPermissions datasetUserPermissions =
                    DatasetUserPermissionValidator.confirmUserDatasetPermissions(
                            userInfo, oldDatasetPermissionList);
            if (!datasetUserPermissions.isWritable()) {
                logger.info(
                        "user does not have delete dataset permission, user: {}, datasetId: {}",
                        userInfo,
                        datasetId);
                throw new DatasetException(
                        "user does not have delete dataset permission, datasetId: " + datasetId);
            }

            DatasetUserPermissions datasetOtherUserPermissions =
                    DatasetUserPermissionValidator.confirmOtherUserDatasetPermissions(
                            userInfo, oldDatasetPermissionList);
            if (datasetOtherUserPermissions.isWritable()
                    || datasetOtherUserPermissions.isReadable()
                    || datasetOtherUserPermissions.isUsable()) {
                logger.info(
                        "the dataset has been authorized to other users, datasetId: {}", datasetId);
                throw new DatasetException(
                        "the dataset has been authorized to other users, datasetId: " + datasetId);
            }
        }

        transactionalDeleteDatasetList(datasetIdList);

        return datasetList;
    }

    @Transactional(rollbackFor = Exception.class)
    public void transactionalDeleteDatasetList(List<String> datasetIdList) {

        if (datasetIdList == null || datasetIdList.isEmpty()) {
            return;
        }

        // delete datasets from database
        for (String datasetId : datasetIdList) {

            int deleteDatasetCount = datasetMapper.deleteDataset(datasetId);
            if (deleteDatasetCount != 1) {
                logger.warn(
                        "delete dataset failed???, datasetId: {}, deleteDatasetCount: {}",
                        deleteDatasetCount,
                        datasetId);
            }

            int datasetPermissionCount =
                    datasetPermissionMapper.deleteDatasetPermissionListByDatasetId(datasetId);

            logger.info(
                    "delete one dataset success, datasetId: {}, deleteDatasetCount: {}, datasetPermissionCount :{}",
                    datasetId,
                    deleteDatasetCount,
                    datasetPermissionCount);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void transactionalUpdateDatasetList(
            UserInfo userInfo,
            List<Dataset> datasetList,
            List<List<DatasetPermission>> datasetPermissionListList)
            throws DatasetException {

        String user = userInfo.getUser();
        String agency = userInfo.getAgency();

        // check dataset existence
        for (int i = 0; i < datasetList.size(); ++i) {
            Dataset dataset = datasetList.get(i);
            String datasetId = dataset.getDatasetId();

            // query dataset info
            Dataset oldDataset = datasetMapper.getDatasetByDatasetId(datasetId, true);
            if (oldDataset == null) {
                logger.error("the dataset does not exist, datasetId: {}", datasetId);
                throw new DatasetException("the dataset does not exist, datasetId: " + datasetId);
            }

            logger.info(" => datasetId: {}, dataset: {}", datasetId, oldDataset);

            // query dataset permissions info
            List<DatasetPermission> oldDatasetPermissionList =
                    datasetPermissionMapper.queryPermissionListForDataset(
                            datasetId, user, agency, true);

            // permission verification
            DatasetUserPermissions datasetUserPermissions =
                    DatasetUserPermissionValidator.confirmUserDatasetPermissions(
                            userInfo, oldDatasetPermissionList);
            if (!datasetUserPermissions.isWritable()) {
                logger.info(
                        "user does not have update dataset permission, user: {}, datasetId: {}",
                        userInfo,
                        datasetId);
                throw new DatasetException(
                        "user does not have update dataset permission, datasetId: " + datasetId);
            }

            DatasetUserPermissions datasetOtherUserPermissions =
                    DatasetUserPermissionValidator.confirmOtherUserDatasetPermissions(
                            userInfo, oldDatasetPermissionList);
            if (datasetOtherUserPermissions.isWritable()
                    || datasetOtherUserPermissions.isReadable()
                    || datasetOtherUserPermissions.isUsable()) {
                logger.info(
                        "the dataset has been authorized to other users, datasetId: {}", datasetId);
                throw new DatasetException(
                        "the dataset has been authorized to other users, datasetId: " + datasetId);
            }

            List<DatasetPermission> datasetPermissionList = datasetPermissionListList.get(i);

            // update dataset info
            int updateCount = datasetMapper.updateDataset(dataset);
            if (updateCount != 1) {
                logger.warn(
                        "update dataset failed???, dataset: {}, updateCount: {}",
                        dataset,
                        updateCount);
            }

            // remove old dataset permissions
            int deleteDatasetPermissionCount =
                    datasetPermissionMapper.deleteDatasetPermissionListByDatasetId(datasetId);
            // insert into new permissions
            int insertDatasetPermissionCount =
                    datasetPermissionMapper.insertDatasetPermissionList(datasetPermissionList);

            logger.info(
                    "update one dataset success, datasetId: {}, updateCount: {}, deleteDatasetPermissionCount :{}, insertDatasetPermissionCount: {}",
                    datasetId,
                    updateCount,
                    deleteDatasetPermissionCount,
                    insertDatasetPermissionCount);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void transactionalUpdateDatasetList(
            List<Dataset> datasetList, List<List<DatasetPermission>> datasetPermissionListList)
            throws DatasetException {

        // check dataset existence
        for (int i = 0; i < datasetList.size(); ++i) {
            Dataset dataset = datasetList.get(i);
            String datasetId = dataset.getDatasetId();

            logger.info(" update dataset => datasetId: {}, dataset: {}", datasetId, dataset);

            List<DatasetPermission> datasetPermissionList = datasetPermissionListList.get(i);

            // update dataset info
            int updateCount = datasetMapper.updateDataset(dataset);
            if (updateCount != 1) {
                logger.warn(
                        "update dataset failed???, dataset: {}, updateCount: {}",
                        dataset,
                        updateCount);
            }

            // remove old dataset permissions
            int deleteDatasetPermissionCount =
                    datasetPermissionMapper.deleteDatasetPermissionListByDatasetId(datasetId);
            // insert into new permissions
            int insertDatasetPermissionCount =
                    datasetPermissionMapper.insertDatasetPermissionList(datasetPermissionList);

            logger.info(
                    "update one dataset success, datasetId: {}, updateCount: {}, deleteDatasetPermissionCount :{}, insertDatasetPermissionCount: {}",
                    datasetId,
                    updateCount,
                    deleteDatasetPermissionCount,
                    insertDatasetPermissionCount);
        }
    }

    /** @param datasetAuthContentList */
    @Transactional(rollbackFor = Exception.class)
    public void transactionalAuthDatasetPermissionList(
            UserInfo userInfo, List<DatasetAuthContent> datasetAuthContentList)
            throws DatasetException {
        for (DatasetAuthContent datasetAuthContent : datasetAuthContentList) {

            String datasetId = datasetAuthContent.getDatasetId();
            Integer permissionType = datasetAuthContent.getPermissionType();
            String authTime = datasetAuthContent.getAuthTime();
            transactionalAuthDatasetPermission(datasetId, permissionType, userInfo, authTime);
        }
    }

    /**
     * @param datasetId
     * @param permissionType
     * @param authorizedUser
     * @param authTime
     */
    @Transactional(rollbackFor = Exception.class)
    public void transactionalAuthDatasetPermission(
            String datasetId, int permissionType, UserInfo authorizedUser, String authTime)
            throws DatasetException {

        DatasetConstant.DatasetPermissionType.isValidDatasetPermissionType(permissionType);

        String oldDatasetId = datasetMapper.getDatasetId(datasetId);
        if (oldDatasetId == null) {
            logger.error(
                    "dataset permission authorize to user failed, dataset not exist, datasetId: {}",
                    datasetId);
            throw new DatasetException(
                    "dataset permission authorize to user failed, dataset not exist, datasetId: "
                            + datasetId);
        }

        DatasetUserPermissions datasetUserPermissions =
                DatasetUserPermissionValidator.confirmUserDatasetPermissions(
                        datasetId, authorizedUser, datasetPermissionMapper, true);
        if (datasetUserPermissions.hasPermission(permissionType)) {
            logger.warn(
                    "dataset permission has been authorized to the user, datasetId: {}, permissionType: {}, authorizedUser: {}, authTime: {}",
                    datasetId,
                    permissionType,
                    authorizedUser,
                    authTime);
            return;
        }

        String user = authorizedUser.getUser();
        String agency = authorizedUser.getAgency();
        String subjectStr = DatasetPermissionUtils.toSubjectStr(user, agency);

        DatasetPermission datasetPermission =
                DatasetPermission.createEntity(
                        datasetId,
                        permissionType,
                        DatasetConstant.DatasetPermissionScope.USER.getValue(),
                        subjectStr,
                        authTime);

        List<DatasetPermission> insertDatasetPermissionList =
                Collections.singletonList(datasetPermission);

        int insertCount =
                datasetPermissionMapper.insertDatasetPermissionList(insertDatasetPermissionList);
        if (insertCount != insertDatasetPermissionList.size()) {
            logger.warn(
                    "insert dataset permission failed, datasetId: {}, permissionType: {}, authorizedUser: {}, authTime: {}, insertCount: {}",
                    datasetId,
                    permissionType,
                    authorizedUser,
                    authTime,
                    insertCount);
        } else {
            logger.info(
                    "insert dataset permission success, datasetId: {}, permissionType: {}, authorizedUser: {}, authTime: {}, insertCount: {}",
                    datasetId,
                    permissionType,
                    authorizedUser,
                    authTime,
                    insertCount);
        }
    }

    /**
     * @param datasetId
     * @param permissionType
     * @param authorizedUser
     */
    @Transactional(rollbackFor = Exception.class)
    public void transactionalRevokeAuthDatasetPermission(
            String datasetId, int permissionType, UserInfo authorizedUser) throws DatasetException {

        DatasetConstant.DatasetPermissionType.isValidDatasetPermissionType(permissionType);

        String oldDatasetId = datasetMapper.getDatasetId(datasetId);
        if (oldDatasetId == null) {
            logger.error("dataset does not exist, datasetId: {}", datasetId);
            throw new DatasetException("dataset not exist, datasetId: " + datasetId);
        }

        DatasetUserPermissions datasetUserPermissions =
                DatasetUserPermissionValidator.confirmUserDatasetPermissions(
                        datasetId, authorizedUser, datasetPermissionMapper, true);
        if (!datasetUserPermissions.hasPermission(permissionType)) {
            logger.warn(
                    "dataset permission has not been authorized to the user, datasetId: {}, permissionType: {}, authorizedUser: {}",
                    datasetId,
                    permissionType,
                    authorizedUser);
            return;
        }

        String user = authorizedUser.getUser();
        String agency = authorizedUser.getAgency();
        String subjectStr = DatasetPermissionUtils.toSubjectStr(user, agency);

        DatasetPermission datasetPermission =
                DatasetPermission.createEntity(
                        datasetId,
                        permissionType,
                        DatasetConstant.DatasetPermissionScope.USER.getValue(),
                        subjectStr);

        int deleteCount = datasetPermissionMapper.deleteDatasetPermission(datasetPermission);
        if (deleteCount != 1) {
            logger.warn("revoke dataset permission failed, deleteCount: {}", deleteCount);
        } else {
            logger.info("revoke dataset permission success, deleteCount: {}", deleteCount);
        }
    }
}
