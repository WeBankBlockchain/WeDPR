package com.webank.wedpr.components.dataset.permission;

import com.webank.wedpr.components.dataset.common.DatasetConstant.DatasetPermissionScope;
import com.webank.wedpr.components.dataset.common.DatasetConstant.DatasetPermissionType;
import com.webank.wedpr.components.dataset.dao.DatasetPermission;
import com.webank.wedpr.components.dataset.dao.DatasetUserPermissions;
import com.webank.wedpr.components.dataset.dao.UserInfo;
import com.webank.wedpr.components.dataset.exception.DatasetException;
import com.webank.wedpr.components.dataset.mapper.DatasetPermissionMapper;
import com.webank.wedpr.components.dataset.utils.TimeUtils;
import com.webank.wedpr.components.token.auth.model.GroupInfo;
import java.util.List;
import org.apache.commons.math3.util.Pair;
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
                DatasetPermissionType.VISIBLE.getType(), userInfo, datasetPermissionList);
    }

    public static boolean hasUserReadablePermission(
            UserInfo userInfo, List<DatasetPermission> datasetPermissionList) {
        return hasUserXxxPermission(
                DatasetPermissionType.READABLE.getType(), userInfo, datasetPermissionList);
    }

    public static boolean hasUserWritablePermission(
            UserInfo userInfo, List<DatasetPermission> datasetPermissionList) {
        return hasUserXxxPermission(
                DatasetPermissionType.WRITABLE.getType(), userInfo, datasetPermissionList);
    }

    public static boolean hasUserUsablePermission(
            UserInfo userInfo, List<DatasetPermission> datasetPermissionList) {
        return hasUserXxxPermission(
                DatasetPermissionType.USABLE.getType(), userInfo, datasetPermissionList);
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
            if ((authTime != null) && TimeUtils.isDateExpired(authTime)) {
                logger.info(
                        "dataset permission has been expired, datasetId: {}, permission: {}",
                        datasetPermission.getDatasetId(),
                        datasetPermission);
                continue;
            }

            String permissionScope = datasetPermission.getPermissionScope();
            String permissionSubjectId = datasetPermission.getPermissionSubjectId();
            if (permissionScope.equalsIgnoreCase(DatasetPermissionScope.GLOBAL.getValue())) {
                return true;
            }

            if (permissionScope.equalsIgnoreCase(DatasetPermissionScope.AGENCY.getValue())
                    && permissionSubjectId.equals(agency)) {
                return true;
            }

            Pair<String, String> stringPair =
                    DatasetPermissionUtils.fromSubjectStr(permissionSubjectId);

            String subjectAgency = stringPair.getFirst();
            String subjectId = stringPair.getSecond();

            if (permissionScope.equalsIgnoreCase(DatasetPermissionScope.USER_GROUP.getValue())) {
                if (groupInfoList != null) {
                    for (GroupInfo groupInfo : groupInfoList) {
                        String userGroupId = groupInfo.getGroupId();
                        if (subjectId.equals(userGroupId)) {
                            return true;
                        }
                    }
                }
            }

            if (permissionScope.equalsIgnoreCase(DatasetPermissionScope.USER.getValue())) {
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
                DatasetPermissionType.VISIBLE.getType(), userInfo, datasetPermissionList);
    }

    public static boolean hasOtherUserWritablePermission(
            UserInfo userInfo, List<DatasetPermission> datasetPermissionList) {
        return hasOtherUserXxxPermission(
                DatasetPermissionType.WRITABLE.getType(), userInfo, datasetPermissionList);
    }

    public static boolean hasOtherUserReadablePermission(
            UserInfo userInfo, List<DatasetPermission> datasetPermissionList) {
        return hasOtherUserXxxPermission(
                DatasetPermissionType.READABLE.getType(), userInfo, datasetPermissionList);
    }

    public static boolean hasOtherUserUsablePermission(
            UserInfo userInfo, List<DatasetPermission> datasetPermissionList) {
        return hasOtherUserXxxPermission(
                DatasetPermissionType.USABLE.getType(), userInfo, datasetPermissionList);
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
            if (authTime != null && TimeUtils.isDateExpired(authTime)) {
                logger.info(
                        "dataset permission has been expired, datasetId: {}, permission: {}",
                        datasetPermission.getDatasetId(),
                        datasetPermission);
                continue;
            }

            String permissionScope = datasetPermission.getPermissionScope();
            String permissionSubjectId = datasetPermission.getPermissionSubjectId();
            if (permissionScope.equalsIgnoreCase(DatasetPermissionScope.GLOBAL.getValue())) {
                return true;
            }

            if (permissionScope.equalsIgnoreCase(DatasetPermissionScope.AGENCY.getValue())) {
                return true;
            }

            if (permissionScope.equalsIgnoreCase(DatasetPermissionScope.USER_GROUP.getValue())) {
                return true;
            }

            Pair<String, String> stringPair =
                    DatasetPermissionUtils.fromSubjectStr(permissionSubjectId);
            String subjectAgency = stringPair.getFirst();
            String subjectId = stringPair.getSecond();

            if (permissionScope.equalsIgnoreCase(DatasetPermissionScope.USER.getValue())) {
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
