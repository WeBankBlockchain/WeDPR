package com.webank.wedpr.components.dataset.permission;

import com.webank.wedpr.components.dataset.utils.JsonUtils;
import com.webank.wedpr.components.db.mapper.dataset.common.DatasetConstant;
import com.webank.wedpr.components.db.mapper.dataset.dao.DatasetPermission;
import com.webank.wedpr.components.db.mapper.dataset.dao.DatasetVisibilityDetails;
import com.webank.wedpr.components.db.mapper.dataset.dao.UserInfo;
import com.webank.wedpr.components.db.mapper.dataset.exception.DatasetException;
import com.webank.wedpr.components.db.mapper.dataset.permission.DatasetPermissionUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DatasetPermissionGenerator {

    private DatasetPermissionGenerator() {}

    /**
     * generate dataset permissions for the owner user
     *
     * @param datasetId
     * @param userInfo
     * @return
     */
    public static List<DatasetPermission> generateDatasetPermissionsForOwner(
            String datasetId, UserInfo userInfo) {

        String user = userInfo.getUser();
        String agency = userInfo.getAgency();

        String subjectStr = DatasetPermissionUtils.toSubjectStr(user, agency);

        List<DatasetPermission> datasetPermissionList = new ArrayList<>();

        // Authorize the user the dataset belongs to

        { // visible
            DatasetPermission datasetPermission =
                    DatasetPermission.createEntity(
                            datasetId,
                            DatasetConstant.DatasetPermissionType.VISIBLE.getType(),
                            DatasetConstant.DatasetPermissionScope.USER.getValue(),
                            subjectStr);
            datasetPermissionList.add(datasetPermission);
        }

        { // writable
            DatasetPermission datasetPermission =
                    DatasetPermission.createEntity(
                            datasetId,
                            DatasetConstant.DatasetPermissionType.WRITABLE.getType(),
                            DatasetConstant.DatasetPermissionScope.USER.getValue(),
                            subjectStr);
            datasetPermissionList.add(datasetPermission);
        }

        { // readable
            DatasetPermission datasetPermission =
                    DatasetPermission.createEntity(
                            datasetId,
                            DatasetConstant.DatasetPermissionType.READABLE.getType(),
                            DatasetConstant.DatasetPermissionScope.USER.getValue(),
                            subjectStr);
            datasetPermissionList.add(datasetPermission);
        }

        { // usable
            DatasetPermission datasetPermission =
                    DatasetPermission.createEntity(
                            datasetId,
                            DatasetConstant.DatasetPermissionType.USABLE.getType(),
                            DatasetConstant.DatasetPermissionScope.USER.getValue(),
                            subjectStr);
            datasetPermissionList.add(datasetPermission);
        }

        return datasetPermissionList;
    }

    /**
     * generate dataset permissions information based on visibility description
     *
     * @param datasetId
     * @param userInfo
     * @param strDatasetVisibilityDesc
     * @param isOwner
     * @return
     */
    public static List<DatasetPermission> generateDatasetVisibilityPermissions(
            int datasetVisibility,
            String datasetId,
            UserInfo userInfo,
            String strDatasetVisibilityDesc,
            boolean isOwner)
            throws DatasetException {

        List<DatasetPermission> datasetPermissionList = null;
        if (isOwner) {
            datasetPermissionList = generateDatasetPermissionsForOwner(datasetId, userInfo);
        } else {
            datasetPermissionList = new ArrayList<>();
        }

        if (datasetVisibility == DatasetConstant.DatasetVisibilityType.PRIVATE.getValue()) {
            return datasetPermissionList;
        }

        DatasetVisibilityDetails datasetVisibilityDetails =
                (DatasetVisibilityDetails)
                        JsonUtils.jsonString2Object(
                                strDatasetVisibilityDesc, DatasetVisibilityDetails.class);

        String agency = userInfo.getAgency();
        boolean global = datasetVisibilityDetails.isGlobal();
        boolean selfAgency = datasetVisibilityDetails.isSelfAgency();
        boolean selfUserGroup = datasetVisibilityDetails.isSelfUserGroup();
        List<String> groupIdList = datasetVisibilityDetails.getGroupIdList();

        if (global) {
            // global visible
            DatasetPermission datasetPermission =
                    DatasetPermission.createEntity(
                            datasetId,
                            DatasetConstant.DatasetPermissionType.VISIBLE.getType(),
                            DatasetConstant.DatasetPermissionScope.GLOBAL.getValue(),
                            "");
            datasetPermissionList.add(datasetPermission);
        } else {

            if (selfAgency) {
                // self agency visible
                DatasetPermission datasetPermission =
                        DatasetPermission.createEntity(
                                datasetId,
                                DatasetConstant.DatasetPermissionType.VISIBLE.getType(),
                                DatasetConstant.DatasetPermissionScope.AGENCY.getValue(),
                                agency);
                datasetPermissionList.add(datasetPermission);
            }

            if (isOwner && selfUserGroup && (groupIdList != null)) {
                // distinct
                List<String> distinctUserGroupList =
                        groupIdList.stream().distinct().collect(Collectors.toList());

                for (String userGroup : distinctUserGroupList) {
                    String subjectStr = DatasetPermissionUtils.toSubjectStr(userGroup, agency);
                    // self user group visible
                    DatasetPermission datasetPermission =
                            DatasetPermission.createEntity(
                                    datasetId,
                                    DatasetConstant.DatasetPermissionType.VISIBLE.getType(),
                                    DatasetConstant.DatasetPermissionScope.USER_GROUP.getValue(),
                                    subjectStr);
                    datasetPermissionList.add(datasetPermission);
                }
            }

            List<String> agencyList = datasetVisibilityDetails.getAgencyList();
            // agency list
            if (agencyList != null && !agencyList.isEmpty()) {
                List<String> distinctAgencyList =
                        agencyList.stream().distinct().collect(Collectors.toList());

                for (String tempAgency : distinctAgencyList) {
                    DatasetPermission datasetPermission =
                            DatasetPermission.createEntity(
                                    datasetId,
                                    DatasetConstant.DatasetPermissionType.VISIBLE.getType(),
                                    DatasetConstant.DatasetPermissionScope.AGENCY.getValue(),
                                    tempAgency);

                    datasetPermissionList.add(datasetPermission);
                }
            }

            List<DatasetVisibilityDetails.AgencyUser> agencyUserList =
                    datasetVisibilityDetails.getUserList();
            // user list
            if (agencyUserList != null && !agencyUserList.isEmpty()) {
                List<DatasetVisibilityDetails.AgencyUser> distinctUserList =
                        agencyUserList.stream().distinct().collect(Collectors.toList());

                for (DatasetVisibilityDetails.AgencyUser agencyUser : distinctUserList) {

                    String subjectStr =
                            DatasetPermissionUtils.toSubjectStr(
                                    agencyUser.getUser(), agencyUser.getAgency());

                    DatasetPermission datasetPermission =
                            DatasetPermission.createEntity(
                                    datasetId,
                                    DatasetConstant.DatasetPermissionType.VISIBLE.getType(),
                                    DatasetConstant.DatasetPermissionScope.USER.getValue(),
                                    subjectStr);
                    datasetPermissionList.add(datasetPermission);
                }
            }
        }

        return datasetPermissionList;
    }
}
