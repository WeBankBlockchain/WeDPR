package com.webank.wedpr.components.dataset.controller;

import com.webank.wedpr.components.dataset.message.QueryAuthRequest;
import com.webank.wedpr.components.dataset.service.DatasetAuthApi;
import com.webank.wedpr.components.db.mapper.dataset.common.DatasetConstant;
import com.webank.wedpr.components.db.mapper.dataset.dao.DatasetAuthContent;
import com.webank.wedpr.components.db.mapper.dataset.dao.DatasetUserPermissions;
import com.webank.wedpr.components.db.mapper.dataset.dao.UserInfo;
import com.webank.wedpr.components.db.mapper.dataset.exception.DatasetException;
import com.webank.wedpr.core.utils.Common;
import com.webank.wedpr.core.utils.Constant;
import com.webank.wedpr.core.utils.WeDPRResponse;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(DatasetConstant.WEDPR_DATASET_API_PREFIX)
@ConditionalOnProperty(value = "wedpr.dataset.debugMode", havingValue = "true")
@Data
public class DatasetAuthController {

    private static final Logger logger = LoggerFactory.getLogger(DatasetAuthController.class);

    @Autowired
    @Qualifier("datasetAuth")
    DatasetAuthApi datasetAuth;

    @PostMapping("auth")
    public WeDPRResponse authorizeDatasetPermission(
            HttpServletRequest httpServletRequest,
            @RequestBody DatasetAuthContent datasetAuthContent) {

        logger.info("auth dataset permission begin, request: {}", datasetAuthContent);

        WeDPRResponse weDPRResponse =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);

        try {
            String datasetId = datasetAuthContent.getDatasetId();
            Common.requireNonEmpty("datasetId", datasetId);
            String user = datasetAuthContent.getUser();
            Common.requireNonEmpty("user", user);
            String agency = datasetAuthContent.getAgency();
            Common.requireNonEmpty("agency", agency);
            String authTime = datasetAuthContent.getAuthTime();
            Common.requireNonEmpty("authTime", authTime);

            Integer permissionType = datasetAuthContent.getPermissionType();
            if (permissionType == null) {
                permissionType = DatasetConstant.DatasetPermissionType.USABLE.getType();
            } else {
                DatasetConstant.DatasetPermissionType.isValidDatasetPermissionType(permissionType);
            }

            String id = UUID.randomUUID().toString();
            UserInfo userInfo =
                    UserInfo.builder().user(user).groupInfos(null).agency(agency).build();

            datasetAuth.authorizeDatasetPermission(
                    id, datasetId, permissionType, authTime, userInfo);

            logger.info("auth dataset permission success, request: {}", datasetAuthContent);

        } catch (DatasetException datasetException) {
            weDPRResponse.setCode(Constant.WEDPR_FAILED);
            weDPRResponse.setMsg(datasetException.getMessage());

            logger.error(
                    "auth dataset permission failed, request: {}, datasetException: ",
                    datasetAuthContent,
                    datasetException);
        } catch (Exception e) {
            weDPRResponse.setCode(Constant.WEDPR_FAILED);
            weDPRResponse.setMsg(e.getMessage());

            logger.error("auth dataset permission failed, request: {}, e: ", datasetAuthContent, e);
        }

        return weDPRResponse;
    }

    @PostMapping("revokeAuth")
    public WeDPRResponse revokeDatasetPermission(
            HttpServletRequest httpServletRequest,
            @RequestBody DatasetAuthContent datasetAuthContent) {

        logger.info("revoke dataset permission begin, request: {}", datasetAuthContent);

        WeDPRResponse weDPRResponse =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        try {
            String datasetId = datasetAuthContent.getDatasetId();
            Common.requireNonEmpty("datasetId", datasetId);
            String user = datasetAuthContent.getUser();
            Common.requireNonEmpty("user", user);
            String agency = datasetAuthContent.getAgency();
            Common.requireNonEmpty("agency", agency);

            Integer permissionType = datasetAuthContent.getPermissionType();
            if (permissionType == null) {
                permissionType = DatasetConstant.DatasetPermissionType.USABLE.getType();
            } else {
                DatasetConstant.DatasetPermissionType.isValidDatasetPermissionType(permissionType);
            }

            String id = UUID.randomUUID().toString();

            UserInfo userInfo =
                    UserInfo.builder().user(user).groupInfos(null).agency(agency).build();

            datasetAuth.revokeDatasetPermission(id, datasetId, permissionType, userInfo);

            logger.info("revoke dataset permission success, request: {}", datasetAuthContent);

        } catch (DatasetException datasetException) {
            weDPRResponse.setCode(Constant.WEDPR_FAILED);
            weDPRResponse.setMsg(datasetException.getMessage());

            logger.error(
                    "revoke dataset permission failed, request: {}, datasetException: ",
                    datasetAuthContent,
                    datasetException);
        } catch (Exception e) {
            weDPRResponse.setCode(Constant.WEDPR_FAILED);
            weDPRResponse.setMsg(e.getMessage());

            logger.error(
                    "revoke dataset permission failed, request: {}, e: ", datasetAuthContent, e);
        }

        return weDPRResponse;
    }

    @PostMapping("queryAuth")
    public WeDPRResponse queryDatasetPermission(
            HttpServletRequest httpServletRequest, @RequestBody QueryAuthRequest queryAuthRequest) {

        logger.info("query dataset permission begin, request: {}", queryAuthRequest);

        WeDPRResponse weDPRResponse =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        try {
            String datasetId = queryAuthRequest.getDatasetId();
            Common.requireNonEmpty("datasetId", datasetId);
            String user = queryAuthRequest.getUser();
            Common.requireNonEmpty("user", user);
            String agency = queryAuthRequest.getAgency();
            Common.requireNonEmpty("agency", agency);
            String userGroup = queryAuthRequest.getUserGroup();
            if (userGroup == null) {
                userGroup = "";
            }

            DatasetUserPermissions datasetUserPermissions =
                    datasetAuth.queryUserPermissions(datasetId, user, userGroup, agency);
            weDPRResponse.setData(datasetUserPermissions);

            logger.info("query dataset permission success, request: {}", queryAuthRequest);

        } catch (DatasetException datasetException) {
            weDPRResponse.setCode(Constant.WEDPR_FAILED);
            weDPRResponse.setMsg(datasetException.getMessage());

            logger.error(
                    "query dataset permission failed, request: {}, datasetException: ",
                    queryAuthRequest,
                    datasetException);
        } catch (Exception e) {
            weDPRResponse.setCode(Constant.WEDPR_FAILED);
            weDPRResponse.setMsg(e.getMessage());

            logger.error("query dataset permission failed, request: {}, e: ", queryAuthRequest, e);
        }

        return weDPRResponse;
    }
}
