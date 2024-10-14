package com.webank.wedpr.components.db.mapper.dataset.permission;

import com.webank.wedpr.common.utils.Common;
import com.webank.wedpr.components.db.mapper.dataset.common.DatasetConstant;
import com.webank.wedpr.components.db.mapper.dataset.dao.DatasetPermission;
import com.webank.wedpr.components.db.mapper.dataset.dao.DatasetUserPermissions;
import com.webank.wedpr.components.db.mapper.dataset.dao.UserInfo;
import com.webank.wedpr.components.db.mapper.dataset.exception.DatasetException;
import com.webank.wedpr.components.db.mapper.dataset.mapper.DatasetPermissionMapper;
import com.webank.wedpr.components.token.auth.model.GroupInfo;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatasetUserPermissionValidator {

    private static final Logger logger =
            LoggerFactory.getLogger(DatasetUserPermissionValidator.class);

    private DatasetUserPermissionValidator() {}

    /**
     * confirm user’s dataset permissions
     *
     * @param datasetId
     * @param userName
     * @param agencyName
     * @param datasetPermissionMapper
     * @param isTx
     * @return
     * @throws DatasetException
     */
    public static DatasetUserPermissions confirmUserDatasetPermissions(
            String datasetId,
            String userName,
            String agencyName,
            DatasetPermissionMapper datasetPermissionMapper,
            boolean isTx)
            throws DatasetException {

        UserInfo userInfo =
                UserInfo.builder().user(userName).agency(agencyName).groupInfos(null).build();
        return confirmUserDatasetPermissions(datasetId, userInfo, datasetPermissionMapper, isTx);
    }

    /**
     * confirm user’s dataset permissions
     *
     * @param datasetId
     * @param userInfo
     * @param datasetPermissionMapper
     * @param isTx
     * @return
     */
    public static DatasetUserPermissions confirmUserDatasetPermissions(
            String datasetId,
            UserInfo userInfo,
            DatasetPermissionMapper datasetPermissionMapper,
            boolean isTx)
            throws DatasetException {

        String user = userInfo.getUser();
        String agency = userInfo.getAgency();
        try {
            List<DatasetPermission> datasetPermissionList =
                    datasetPermissionMapper.queryPermissionListForDataset(
                            datasetId, user, agency, isTx);

            return confirmUserDatasetPermissions(userInfo, datasetPermissionList);
        } catch (Exception e) {
            logger.error(
                    "query permission list db operation exception, datasetId: {}, userInfo: {}, e: ",
                    datasetId,
                    userInfo,
                    e);
            throw new DatasetException(
                    "query permission list db operation exception, " + e.getMessage());
        }
    }

    public static DatasetUserPermissions confirmUserDatasetPermissions(
            UserInfo userInfo, List<DatasetPermission> datasetPermissionList) {

        boolean visible = hasUserVisiblePermission(userInfo, datasetPermissionList);
        boolean readable = hasUserReadablePermission(userInfo, datasetPermissionList);
        boolean writable = hasUserWritablePermission(userInfo, datasetPermissionList);
        boolean usable = hasUserUsablePermission(userInfo, datasetPermissionList);

        return DatasetUserPermissions.builder()
                .visible(visible)
                .readable(readable)
                .writable(writable)
                .usable(usable)
                .build();
    }

    public static boolean hasUserVisiblePermission(
            UserInfo userInfo, List<DatasetPermission> datasetPermissionList) {
        return hasUserXxxPermission(
                DatasetConstant.DatasetPermissionType.VISIBLE.getType(),
                userInfo,
                datasetPermissionList);
    }

    public static boolean hasUserReadablePermission(
            UserInfo userInfo, List<DatasetPermission> datasetPermissionList) {
        return hasUserXxxPermission(
                DatasetConstant.DatasetPermissionType.READABLE.getType(),
                userInfo,
                datasetPermissionList);
    }

    public static boolean hasUserWritablePermission(
            UserInfo userInfo, List<DatasetPermission> datasetPermissionList) {
        return hasUserXxxPermission(
                DatasetConstant.DatasetPermissionType.WRITABLE.getType(),
                userInfo,
                datasetPermissionList);
    }

    public static boolean hasUserUsablePermission(
            UserInfo userInfo, List<DatasetPermission> datasetPermissionList) {
        return hasUserXxxPermission(
                DatasetConstant.DatasetPermissionType.USABLE.getType(),
                userInfo,
                datasetPermissionList);
    }

    public static boolean hasUserXxxPermission(
            int permissionType, UserInfo userInfo, List<DatasetPermission> datasetPermissionList) {

        String user = userInfo.getUser();
        String agency = userInfo.getAgency();
        List<GroupInfo> groupInfoList = userInfo.getGroupInfos();

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "has user xxx permission, permissionType: {}, userInfo: {}, datasetPermissionList: {}",
                    permissionType,
                    userInfo,
                    datasetPermissionList);
        }

        for (DatasetPermission datasetPermission : datasetPermissionList) {
            if (permissionType != datasetPermission.getPermissionType()) {
                continue;
            }

            // check if expired
            String authTime = datasetPermission.getAuthTime();
            if ((authTime != null) && Common.isDateExpired(authTime)) {
                logger.info(
                        "dataset permission has been expired, datasetId: {}, permission: {}",
                        datasetPermission.getDatasetId(),
                        datasetPermission);
                continue;
            }

            String permissionScope = datasetPermission.getPermissionScope();
            String permissionSubjectId = datasetPermission.getPermissionSubjectId();
            if (permissionScope.equalsIgnoreCase(
                    DatasetConstant.DatasetPermissionScope.GLOBAL.getValue())) {
                return true;
            }

            if (permissionScope.equalsIgnoreCase(
                            DatasetConstant.DatasetPermissionScope.AGENCY.getValue())
                    && permissionSubjectId.equals(agency)) {
                return true;
            }

            Pair<String, String> stringPair =
                    DatasetPermissionUtils.fromSubjectStr(permissionSubjectId);

            String subjectAgency = stringPair.getLeft();
            String subjectId = stringPair.getRight();

            if (permissionScope.equalsIgnoreCase(
                    DatasetConstant.DatasetPermissionScope.USER_GROUP.getValue())) {
                if (groupInfoList != null) {
                    for (GroupInfo groupInfo : groupInfoList) {
                        String userGroupId = groupInfo.getGroupId();
                        if (subjectId.equals(userGroupId)) {
                            return true;
                        }
                    }
                }
            }

            if (permissionScope.equalsIgnoreCase(
                    DatasetConstant.DatasetPermissionScope.USER.getValue())) {
                if (subjectAgency == null && user.equals(subjectId)) {
                    return true;
                }

                if (subjectAgency != null
                        && user.equals(subjectId)
                        && subjectAgency.equals(agency)) {
                    return true;
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("has user xxx permission ret false, permissionType: {}", permissionType);
        }

        return false;
    }

    /**
     * Checks whether users other than user have permissions on the dataset
     *
     * @param userInfo
     * @param datasetPermissionList
     * @return
     */
    public static DatasetUserPermissions confirmOtherUserDatasetPermissions(
            UserInfo userInfo, List<DatasetPermission> datasetPermissionList) {

        boolean visible = hasOtherUserVisiblePermission(userInfo, datasetPermissionList);
        boolean readable = hasOtherUserReadablePermission(userInfo, datasetPermissionList);
        boolean writable = hasOtherUserWritablePermission(userInfo, datasetPermissionList);
        boolean usable = hasOtherUserUsablePermission(userInfo, datasetPermissionList);

        return DatasetUserPermissions.builder()
                .visible(visible)
                .readable(readable)
                .writable(writable)
                .usable(usable)
                .build();
    }

    public static boolean hasOtherUserVisiblePermission(
            UserInfo userInfo, List<DatasetPermission> datasetPermissionList) {
        return hasOtherUserXxxPermission(
                DatasetConstant.DatasetPermissionType.VISIBLE.getType(),
                userInfo,
                datasetPermissionList);
    }

    public static boolean hasOtherUserWritablePermission(
            UserInfo userInfo, List<DatasetPermission> datasetPermissionList) {
        return hasOtherUserXxxPermission(
                DatasetConstant.DatasetPermissionType.WRITABLE.getType(),
                userInfo,
                datasetPermissionList);
    }

    public static boolean hasOtherUserReadablePermission(
            UserInfo userInfo, List<DatasetPermission> datasetPermissionList) {
        return hasOtherUserXxxPermission(
                DatasetConstant.DatasetPermissionType.READABLE.getType(),
                userInfo,
                datasetPermissionList);
    }

    public static boolean hasOtherUserUsablePermission(
            UserInfo userInfo, List<DatasetPermission> datasetPermissionList) {
        return hasOtherUserXxxPermission(
                DatasetConstant.DatasetPermissionType.USABLE.getType(),
                userInfo,
                datasetPermissionList);
    }

    public static boolean hasOtherUserXxxPermission(
            int permissionType, UserInfo userInfo, List<DatasetPermission> datasetPermissionList) {
        String user = userInfo.getUser();
        String agency = userInfo.getAgency();

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "has other user xxx permission, permissionType: {}, userInfo: {}, datasetPermissionList: {}",
                    permissionType,
                    userInfo,
                    datasetPermissionList);
        }

        for (DatasetPermission datasetPermission : datasetPermissionList) {
            if (permissionType != datasetPermission.getPermissionType()) {
                continue;
            }

            // check if expired
            String authTime = datasetPermission.getAuthTime();
            if (authTime != null && Common.isDateExpired(authTime)) {
                logger.info(
                        "dataset permission has been expired, datasetId: {}, permission: {}",
                        datasetPermission.getDatasetId(),
                        datasetPermission);
                continue;
            }

            String permissionScope = datasetPermission.getPermissionScope();
            String permissionSubjectId = datasetPermission.getPermissionSubjectId();
            if (permissionScope.equalsIgnoreCase(
                    DatasetConstant.DatasetPermissionScope.GLOBAL.getValue())) {
                return true;
            }

            if (permissionScope.equalsIgnoreCase(
                    DatasetConstant.DatasetPermissionScope.AGENCY.getValue())) {
                return true;
            }

            if (permissionScope.equalsIgnoreCase(
                    DatasetConstant.DatasetPermissionScope.USER_GROUP.getValue())) {
                return true;
            }

            Pair<String, String> stringPair =
                    DatasetPermissionUtils.fromSubjectStr(permissionSubjectId);
            String subjectAgency = stringPair.getLeft();
            String subjectId = stringPair.getRight();

            if (permissionScope.equalsIgnoreCase(
                    DatasetConstant.DatasetPermissionScope.USER.getValue())) {
                if (subjectAgency == null && !user.equals(subjectId)) {
                    return true;
                }

                if (subjectAgency != null
                        && !(user.equals(subjectId) && subjectAgency.equals(agency))) {
                    return true;
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "has other user xxx permission ret false, permissionType: {}", permissionType);
        }

        return false;
    }
}
