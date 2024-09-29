package com.webank.wedpr.components.dataset.controller;

import com.webank.wedpr.components.dataset.config.DataSourceTypeConfig;
import com.webank.wedpr.components.dataset.config.DataSourceTypeConfig.LabelValue;
import com.webank.wedpr.components.dataset.config.DatasetConfig;
import com.webank.wedpr.components.dataset.datasource.DataSourceType;
import com.webank.wedpr.components.dataset.message.CreateDatasetRequest;
import com.webank.wedpr.components.dataset.message.CreateDatasetResponse;
import com.webank.wedpr.components.dataset.message.DeleteDatasetListRequest;
import com.webank.wedpr.components.dataset.message.DeleteDatasetRequest;
import com.webank.wedpr.components.dataset.message.ListDatasetResponse;
import com.webank.wedpr.components.dataset.message.QueryDatasetListRequest;
import com.webank.wedpr.components.dataset.message.UpdateDatasetListRequest;
import com.webank.wedpr.components.dataset.message.UpdateDatasetRequest;
import com.webank.wedpr.components.dataset.service.ChunkUploadApi;
import com.webank.wedpr.components.dataset.service.DatasetServiceApi;
import com.webank.wedpr.components.dataset.utils.JsonUtils;
import com.webank.wedpr.components.dataset.utils.UserTokenUtils;
import com.webank.wedpr.components.db.mapper.dataset.common.DatasetConstant;
import com.webank.wedpr.components.db.mapper.dataset.dao.Dataset;
import com.webank.wedpr.components.db.mapper.dataset.dao.DatasetVisibilityDetails;
import com.webank.wedpr.components.db.mapper.dataset.dao.UserInfo;
import com.webank.wedpr.components.db.mapper.dataset.exception.DatasetException;
import com.webank.wedpr.components.db.mapper.dataset.mapper.DatasetPermissionMapper;
import com.webank.wedpr.components.storage.api.FileStorageInterface;
import com.webank.wedpr.components.storage.api.StoragePath;
import com.webank.wedpr.core.utils.Common;
import com.webank.wedpr.core.utils.Constant;
import com.webank.wedpr.core.utils.WeDPRResponse;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(DatasetConstant.WEDPR_DATASET_API_PREFIX)
@Data
public class DatasetController {
    private static final Logger logger = LoggerFactory.getLogger(DatasetController.class);

    @Autowired private DatasetPermissionMapper datasetPermissionMapper;

    @Qualifier("chunkUpload")
    @Autowired
    private ChunkUploadApi chunkUploadApi;

    @Qualifier("datasetService")
    @Autowired
    private DatasetServiceApi datasetService;

    @Qualifier("fileStorage")
    @Autowired
    private FileStorageInterface fileStorage;

    @Autowired private DataSourceTypeConfig dataSourceTypeConfig;

    @Autowired private DatasetConfig datasetConfig;

    @GetMapping("test")
    public String test(HttpServletRequest httpServletRequest) {

        logger.info("测试接口调用.");

        return "Hello, Test!!!";
    }

    @GetMapping("getDataUploadType")
    public WeDPRResponse getDataUploadType(HttpServletRequest httpServletRequest) {

        List<LabelValue> dataSourceTypeList = dataSourceTypeConfig.getDataSourceType();

        WeDPRResponse response =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        response.setData(dataSourceTypeList);
        return response;
    }

    @PostMapping(value = "createDataset")
    public WeDPRResponse createDataset(
            HttpServletRequest httpServletRequest,
            @RequestBody CreateDatasetRequest createDatasetRequest) {

        logger.info("create dataset begin, request: {}", createDatasetRequest);

        long startTimeMillis = System.currentTimeMillis();
        WeDPRResponse weDPRResponse =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);

        // TODO: 检查用户创建数据集的权限

        try {
            UserInfo userInfo = UserTokenUtils.getUserInfo(datasetConfig, httpServletRequest);

            // parameters check
            String datasetTitle = createDatasetRequest.getDatasetTitle();
            Common.requireNonEmpty("datasetTitle", datasetTitle);

            String datasetLabel = createDatasetRequest.getDatasetLabel();
            Common.requireNonEmpty("datasetLabel", datasetLabel);

            String datasetDesc = createDatasetRequest.getDatasetDesc();
            Common.requireNonEmpty("datasetDesc", datasetDesc);

            Integer datasetVisibility = createDatasetRequest.getDatasetVisibility();
            Common.requireNonNull("datasetVisibility", datasetVisibility);
            if (datasetVisibility == DatasetConstant.DatasetVisibilityType.PUBLIC.getValue()) {
                String datasetVisibilityDetails =
                        createDatasetRequest.getDatasetVisibilityDetails();
                Common.requireNonEmpty("datasetVisibilityDetails", datasetVisibilityDetails);
                JsonUtils.jsonString2Object(
                        datasetVisibilityDetails, DatasetVisibilityDetails.class);
            }

            // data source type
            String dataSourceType = createDatasetRequest.getDataSourceType();
            Common.requireNonEmpty("dataSourceType", dataSourceType);
            DataSourceType.isValidDataSourceType(dataSourceType);

            // approval chain info
            String approvalChain = createDatasetRequest.getApprovalChain();
            Common.requireNonEmpty("approvalChain", approvalChain);

            CreateDatasetResponse data =
                    datasetService.createDataset(userInfo, createDatasetRequest);
            weDPRResponse.setData(data);

            long endTimeMillis = System.currentTimeMillis();
            logger.info(
                    "create dataset success, datasetId: {}, cost(ms): {}",
                    data.getDatasetId(),
                    (endTimeMillis - startTimeMillis));

        } catch (DatasetException datasetException) {
            weDPRResponse.setCode(Constant.WEDPR_FAILED);
            weDPRResponse.setMsg(datasetException.getMessage());
            logger.error(
                    "create dataset failed, request: {}, datasetException: ",
                    createDatasetRequest,
                    datasetException);
        } catch (Exception e) {
            weDPRResponse.setCode(Constant.WEDPR_FAILED);
            weDPRResponse.setMsg(e.getMessage());
            logger.error("create dataset failed, request: {}, e: ", createDatasetRequest, e);
        }

        return weDPRResponse;
    }

    @GetMapping("queryDataset")
    public WeDPRResponse queryDataset(
            HttpServletRequest httpServletRequest, @RequestParam("datasetId") String datasetId) {

        WeDPRResponse weDPRResponse =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        try {
            // fetch login user info
            UserInfo userInfo = UserTokenUtils.getUserInfo(datasetConfig, httpServletRequest);

            // query dataset from database
            Dataset dataset = datasetService.queryDataset(userInfo, datasetId);

            weDPRResponse.setData(dataset);

        } catch (DatasetException datasetException) {
            logger.error(
                    "query dataset failed, datasetId: {}, datasetException: ",
                    datasetId,
                    datasetException);
            weDPRResponse.setCode(Constant.WEDPR_FAILED);
            weDPRResponse.setMsg(datasetException.getMessage());
        } catch (Exception e) {
            logger.error("query dataset failed, datasetId: {}, e: ", datasetId, e);
            weDPRResponse.setCode(Constant.WEDPR_FAILED);
            weDPRResponse.setMsg(e.getMessage());
        }

        return weDPRResponse;
    }

    @PostMapping("queryDatasetList")
    public WeDPRResponse queryDatasetList(
            HttpServletRequest httpServletRequest,
            @RequestBody QueryDatasetListRequest queryDatasetListRequest) {

        logger.info("query dataset list begin, request: {}", queryDatasetListRequest);

        long startTimeMillis = System.currentTimeMillis();

        WeDPRResponse weDPRResponse =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        try {
            // fetch login user info
            UserInfo userInfo = UserTokenUtils.getUserInfo(datasetConfig, httpServletRequest);

            List<String> datasetIdList = queryDatasetListRequest.getDatasetIdList();
            // query dataset from database
            List<Dataset> datasetList = datasetService.queryDatasetList(userInfo, datasetIdList);

            weDPRResponse.setData(datasetList);

            long endTimeMillis = System.currentTimeMillis();

            logger.info("query dataset list end, cost(ms): {}", (endTimeMillis - startTimeMillis));

        } catch (DatasetException datasetException) {
            logger.error(
                    "query dataset list failed, request: {}, datasetException: ",
                    queryDatasetListRequest,
                    datasetException);
            weDPRResponse.setCode(Constant.WEDPR_FAILED);
            weDPRResponse.setMsg(datasetException.getMessage());
        } catch (Exception e) {
            logger.error("query dataset list failed, request: {}, e: ", queryDatasetListRequest, e);
            weDPRResponse.setCode(Constant.WEDPR_FAILED);
            weDPRResponse.setMsg(e.getMessage());
        }

        return weDPRResponse;
    }

    @GetMapping("listDataset")
    public WeDPRResponse listDataset(
            HttpServletRequest httpServletRequest,
            @RequestParam(value = "ownerAgencyName", required = false) String ownerAgency,
            @RequestParam(value = "ownerUserName", required = false) String ownerUser,
            @RequestParam(value = "permissionType", required = false) String permissionType,
            @RequestParam(value = "datasetTitle", required = false) String datasetTitle,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {

        WeDPRResponse weDPRResponse =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        try {
            if (startTime != null) {
                Common.isValidDateFormat(startTime);
            }

            if (endTime != null) {
                Common.isValidDateFormat(endTime);
            }

            /*
            "permissions": {
                "readable": true,
                        "visible": true,
                        "usable": true,
                        "writable": true
            },
            */

            int type = DatasetConstant.DatasetPermissionType.VISIBLE.getType();
            if (permissionType != null) {
                type = DatasetConstant.DatasetPermissionType.fromString(permissionType);
            }

            UserInfo userInfo = UserTokenUtils.getUserInfo(datasetConfig, httpServletRequest);

            ListDatasetResponse listDatasetResponse =
                    datasetService.listDataset(
                            userInfo,
                            ownerAgency,
                            ownerUser,
                            datasetTitle,
                            type,
                            startTime,
                            endTime,
                            pageNum,
                            pageSize);
            weDPRResponse.setData(listDatasetResponse);

        } catch (DatasetException datasetException) {
            logger.error("list dataset datasetException, e: ", datasetException);
            weDPRResponse.setCode(Constant.WEDPR_FAILED);
            weDPRResponse.setMsg(datasetException.getMessage());
        } catch (Exception e) {
            logger.error("list dataset Exception, e: ", e);
            weDPRResponse.setCode(Constant.WEDPR_FAILED);
            weDPRResponse.setMsg(e.getMessage());
        }
        return weDPRResponse;
    }

    @PostMapping("deleteDataset")
    public WeDPRResponse deleteDataset(
            HttpServletRequest httpServletRequest,
            @RequestBody DeleteDatasetRequest deleteDatasetRequest) {

        DeleteDatasetListRequest deleteDatasetListRequest = new DeleteDatasetListRequest();
        deleteDatasetListRequest.setDatasetIdList(
                Collections.singletonList(deleteDatasetRequest.getDatasetId()));
        return deleteDatasetList(httpServletRequest, deleteDatasetListRequest);
    }

    @PostMapping("deleteDatasetList")
    public WeDPRResponse deleteDatasetList(
            HttpServletRequest httpServletRequest,
            @RequestBody DeleteDatasetListRequest deleteDatasetListRequest) {

        long startTimeMillis = System.currentTimeMillis();
        logger.info("delete dataset list begin, request: {}", deleteDatasetListRequest);

        WeDPRResponse weDPRResponse =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);

        try {
            UserInfo userInfo = UserTokenUtils.getUserInfo(datasetConfig, httpServletRequest);

            List<String> datasetIdList = deleteDatasetListRequest.getDatasetIdList();
            if (datasetIdList == null || datasetIdList.isEmpty()) {
                throw new DatasetException("illegal params, request set is empty");
            }

            if (datasetIdList.size() > datasetConfig.getMaxBatchSize()) {
                logger.error(
                        "illegal params, delete dataset amount is overflow, maxAllowCount: {}",
                        datasetConfig.getMaxBatchSize());
                throw new DatasetException(
                        "illegal params, delete dataset amount is overflow, maxAllowCount: "
                                + datasetConfig.getMaxBatchSize());
            }

            datasetService.deleteDatasetList(userInfo, deleteDatasetListRequest.getDatasetIdList());

            long endTimeMillis = System.currentTimeMillis();
            logger.info(
                    "delete dataset list success, request: {}, cost(ms): {}",
                    deleteDatasetListRequest,
                    (endTimeMillis - startTimeMillis));
        } catch (DatasetException datasetException) {
            long endTimeMillis = System.currentTimeMillis();
            logger.error(
                    "delete dataset list failed, request: {}, cost(ms): {}, datasetException: ",
                    deleteDatasetListRequest,
                    (endTimeMillis - startTimeMillis),
                    datasetException);

            weDPRResponse.setCode(Constant.WEDPR_FAILED);
            weDPRResponse.setMsg(datasetException.getMessage());
        } catch (Exception e) {
            long endTimeMillis = System.currentTimeMillis();
            logger.error(
                    "delete dataset list failed, request: {}, cost(ms): {}, e: ",
                    deleteDatasetListRequest,
                    (endTimeMillis - startTimeMillis),
                    e);

            weDPRResponse.setCode(Constant.WEDPR_FAILED);
            weDPRResponse.setMsg(e.getMessage());
        }

        return weDPRResponse;
    }

    @PostMapping(value = "updateDataset")
    public WeDPRResponse updateDataset(
            HttpServletRequest httpServletRequest,
            @RequestBody UpdateDatasetRequest updateDatasetRequest) {
        UpdateDatasetListRequest updateDatasetListRequest = new UpdateDatasetListRequest();
        updateDatasetListRequest.setDatasetList(Collections.singletonList(updateDatasetRequest));
        return updateDatasetList(httpServletRequest, updateDatasetListRequest);
    }

    @PostMapping(value = "updateDatasetList")
    public WeDPRResponse updateDatasetList(
            HttpServletRequest httpServletRequest,
            @RequestBody UpdateDatasetListRequest updateDatasetListRequest) {

        long startTimeMillis = System.currentTimeMillis();
        logger.info("update dataset list begin, request: {}", updateDatasetListRequest);

        WeDPRResponse weDPRResponse =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);

        try {

            List<UpdateDatasetRequest> updateDatasetRequestList =
                    updateDatasetListRequest.getDatasetList();
            // Validate parameters
            if (updateDatasetRequestList == null || updateDatasetRequestList.isEmpty()) {
                throw new DatasetException("illegal params, request set is empty");
            }

            if (updateDatasetRequestList.size() > datasetConfig.getMaxBatchSize()) {
                logger.error(
                        "illegal params, update dataset amount is overflow, maxAllowCount: {}",
                        datasetConfig.getMaxBatchSize());
                throw new DatasetException(
                        "illegal params, update dataset amount is overflow, maxAllowCount: "
                                + datasetConfig.getMaxBatchSize());
            }

            for (UpdateDatasetRequest updateDatasetRequest : updateDatasetRequestList) {
                String datasetTitle = updateDatasetRequest.getDatasetTitle();
                String datasetLabel = updateDatasetRequest.getDatasetLabel();
                String datasetDesc = updateDatasetRequest.getDatasetDesc();
                String datasetId = updateDatasetRequest.getDatasetId();
                String approvalChain = updateDatasetRequest.getApprovalChain();

                Common.requireNonEmpty("datasetId", datasetId);
                Common.requireNonEmpty("datasetTitle", datasetTitle);
                Common.requireNonEmpty("datasetLabel", datasetLabel);
                Common.requireNonEmpty("datasetDesc", datasetDesc);

                Integer datasetVisibility = updateDatasetRequest.getDatasetVisibility();
                Common.requireNonNull("datasetVisibility", datasetVisibility);
                if (datasetVisibility == DatasetConstant.DatasetVisibilityType.PUBLIC.getValue()) {
                    String datasetVisibilityDetails =
                            updateDatasetRequest.getDatasetVisibilityDetails();
                    Common.requireNonEmpty("datasetVisibilityDetails", datasetVisibilityDetails);
                    JsonUtils.jsonString2Object(
                            datasetVisibilityDetails, DatasetVisibilityDetails.class);
                }

                Common.requireNonEmpty("approvalChain", approvalChain);
            }

            UserInfo userInfo = UserTokenUtils.getUserInfo(datasetConfig, httpServletRequest);

            datasetService.updateDatasetList(userInfo, updateDatasetRequestList);

            long endTimeMillis = System.currentTimeMillis();
            logger.info(
                    "update dataset list success, request: {}, cost(ms): {}",
                    updateDatasetRequestList,
                    (endTimeMillis - startTimeMillis));

        } catch (DatasetException datasetException) {
            long endTimeMillis = System.currentTimeMillis();
            logger.error(
                    "update dataset list failed, request: {}, cost(ms): {}, datasetException: ",
                    updateDatasetListRequest,
                    (endTimeMillis - startTimeMillis),
                    datasetException);

            weDPRResponse.setCode(Constant.WEDPR_FAILED);
            weDPRResponse.setMsg(datasetException.getMessage());
        } catch (Exception e) {
            long endTimeMillis = System.currentTimeMillis();
            logger.error(
                    "update dataset list failed, request: {}, cost(ms): {}, e: ",
                    updateDatasetListRequest,
                    (endTimeMillis - startTimeMillis),
                    e);

            weDPRResponse.setCode(Constant.WEDPR_FAILED);
            weDPRResponse.setMsg(e.getMessage());
        }

        return weDPRResponse;
    }

    @GetMapping(value = "getDatasetStoragePath")
    public WeDPRResponse getDatasetStoragePath(
            HttpServletRequest httpServletRequest,
            @RequestParam(value = "datasetID", required = true) String datasetID) {

        long startTimeMillis = System.currentTimeMillis();
        logger.info("get dataset storage path begin, datasetID: {}", datasetID);

        WeDPRResponse weDPRResponse =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);

        try {
            StoragePath storagePath = datasetService.getDatasetStoragePath(datasetID);
            weDPRResponse.setData(storagePath);

            long endTimeMillis = System.currentTimeMillis();
            logger.info(
                    "get dataset storage path  success, datasetID: {}, cost(ms): {}",
                    datasetID,
                    (endTimeMillis - startTimeMillis));
        } catch (Exception e) {

            weDPRResponse.setCode(Constant.WEDPR_FAILED);
            weDPRResponse.setMsg(e.getMessage());

            long endTimeMillis = System.currentTimeMillis();
            logger.error(
                    "get dataset storage path failed, datasetID: {}, cost(ms): {}, e: ",
                    datasetID,
                    (endTimeMillis - startTimeMillis),
                    e);
        }

        return weDPRResponse;
    }
}
