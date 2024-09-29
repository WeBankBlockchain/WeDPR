package com.webank.wedpr.components.dataset.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.page.PageMethod;
import com.webank.wedpr.components.dataset.common.DatasetStatus;
import com.webank.wedpr.components.dataset.config.DatasetConfig;
import com.webank.wedpr.components.dataset.datasource.DataSourceMeta;
import com.webank.wedpr.components.dataset.datasource.DataSourceType;
import com.webank.wedpr.components.dataset.datasource.dispatch.DataSourceProcessorDispatcher;
import com.webank.wedpr.components.dataset.datasource.processor.DataSourceProcessor;
import com.webank.wedpr.components.dataset.datasource.processor.DataSourceProcessorContext;
import com.webank.wedpr.components.dataset.datasource.storage.DatasetStoragePathRetriever;
import com.webank.wedpr.components.dataset.message.CreateDatasetRequest;
import com.webank.wedpr.components.dataset.message.CreateDatasetResponse;
import com.webank.wedpr.components.dataset.message.ListDatasetResponse;
import com.webank.wedpr.components.dataset.message.UpdateDatasetRequest;
import com.webank.wedpr.components.dataset.permission.DatasetPermissionGenerator;
import com.webank.wedpr.components.dataset.sync.api.DatasetSyncerApi;
import com.webank.wedpr.components.dataset.utils.ThreadPoolUtils;
import com.webank.wedpr.components.db.mapper.dataset.common.DatasetConstant;
import com.webank.wedpr.components.db.mapper.dataset.dao.Dataset;
import com.webank.wedpr.components.db.mapper.dataset.dao.DatasetPermission;
import com.webank.wedpr.components.db.mapper.dataset.dao.DatasetUserPermissions;
import com.webank.wedpr.components.db.mapper.dataset.dao.UserInfo;
import com.webank.wedpr.components.db.mapper.dataset.exception.DatasetException;
import com.webank.wedpr.components.db.mapper.dataset.mapper.DatasetMapper;
import com.webank.wedpr.components.db.mapper.dataset.mapper.DatasetPermissionMapper;
import com.webank.wedpr.components.db.mapper.dataset.mapper.wapper.DatasetTransactionalWrapper;
import com.webank.wedpr.components.db.mapper.dataset.mapper.wapper.DatasetWrapper;
import com.webank.wedpr.components.db.mapper.dataset.permission.DatasetPermissionUtils;
import com.webank.wedpr.components.db.mapper.dataset.permission.DatasetUserPermissionValidator;
import com.webank.wedpr.components.storage.api.FileStorageInterface;
import com.webank.wedpr.components.storage.api.StoragePath;
import com.webank.wedpr.components.storage.builder.StoragePathBuilder;
import com.webank.wedpr.components.uuid.generator.WeDPRUuidGenerator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("datasetService")
@Data
public class DatasetServiceImpl implements DatasetServiceApi {

    private static final Logger logger = LoggerFactory.getLogger(DatasetServiceImpl.class);

    @Autowired private DatasetConfig datasetConfig;
    @Autowired private DatasetMapper datasetMapper;
    @Autowired private DatasetPermissionMapper datasetPermissionMapper;
    @Autowired private DatasetTransactionalWrapper datasetTransactionalWrapper;
    @Autowired private DataSourceProcessorDispatcher dataSourceProcessorDispatcher;
    @Autowired private DatasetWrapper datasetWrapper;
    @Autowired private DatasetStoragePathRetriever datasetStoragePathRetriever;

    @Qualifier("fileStorage")
    @Autowired
    private FileStorageInterface fileStorage;

    @Qualifier("datasetSyncer")
    @Autowired
    DatasetSyncerApi datasetSyncer;

    @Qualifier("chunkUpload")
    @Autowired
    private ChunkUploadApi chunkUpload;

    @Autowired
    @Qualifier("datasetAsyncExecutor")
    private Executor executor;

    /**
     * create dataset id
     *
     * @return
     */
    public static String newDatasetId() {
        return DatasetConstant.DATASET_ID_PREFIX + WeDPRUuidGenerator.generateID();
    }

    // CreateDatasetRequest => Dataset
    public Dataset constructDataset(
            String datasetId,
            UserInfo userInfo,
            boolean dynamicDataSource,
            CreateDatasetRequest createDatasetRequest) {
        Dataset dataset = new Dataset();

        String datasetVisibilityDetails = createDatasetRequest.getDatasetVisibilityDetails();
        if (datasetVisibilityDetails == null) {
            datasetVisibilityDetails = "";
        }

        String dataSourceMeta = createDatasetRequest.getDataSourceMeta();
        if (dataSourceMeta == null) {
            dataSourceMeta = "";
        }

        dataset.setDatasetId(datasetId);
        dataset.setDatasetTitle(createDatasetRequest.getDatasetTitle());
        dataset.setDatasetLabel(createDatasetRequest.getDatasetLabel());
        dataset.setDatasetDesc(createDatasetRequest.getDatasetDesc());
        dataset.setDataSourceType(createDatasetRequest.getDataSourceType());
        dataset.setDataSourceMeta(dataSourceMeta);
        dataset.setDatasetFields("");
        dataset.setDatasetVersionHash("");
        dataset.setDatasetStorageType("");
        dataset.setDatasetStoragePath("");
        dataset.setDatasetSize(0L);
        dataset.setDatasetRecordCount(0);
        dataset.setDatasetColumnCount(0);
        dataset.setVisibility(createDatasetRequest.getDatasetVisibility());
        dataset.setVisibilityDetails(datasetVisibilityDetails);

        dataset.setOwnerAgencyName(userInfo.getAgency());
        dataset.setOwnerUserName(userInfo.getUser());

        dataset.setApprovalChain(createDatasetRequest.getApprovalChain());

        if (dynamicDataSource) {
            dataset.setStatus(DatasetStatus.Success.getCode());
            dataset.setStatusDesc(DatasetStatus.Success.getMessage());
        } else {
            dataset.setStatus(DatasetStatus.Created.getCode());
            dataset.setStatusDesc(DatasetStatus.Created.getMessage());
        }

        return dataset;
    }

    /**
     * create dataset
     *
     * @param userInfo
     * @param createDatasetRequest
     * @return
     * @throws DatasetException
     */
    @Override
    public CreateDatasetResponse createDataset(
            UserInfo userInfo, CreateDatasetRequest createDatasetRequest) throws DatasetException {
        // generate new dataset id
        String datasetId = newDatasetId();

        logger.info(" => new datasetId: {}", datasetId);

        // visibility permissions
        int datasetVisibility = createDatasetRequest.getDatasetVisibility();
        String datasetVisibilityDetails = createDatasetRequest.getDatasetVisibilityDetails();

        List<DatasetPermission> datasetPermissionList =
                DatasetPermissionGenerator.generateDatasetVisibilityPermissions(
                        datasetVisibility, datasetId, userInfo, datasetVisibilityDetails, true);

        String strDataSourceType = createDatasetRequest.getDataSourceType();
        String strDataSourceMeta = createDatasetRequest.getDataSourceMeta();

        // validates the type
        DataSourceProcessor dataSourceProcessor =
                dataSourceProcessorDispatcher.getDataSourceProcessor(strDataSourceType);
        if (dataSourceProcessor == null) {
            logger.error("Unsupported data source type, dataSourceType: {}", strDataSourceType);
            throw new DatasetException(
                    "Unsupported data source type, dataSourceType: " + strDataSourceType);
        }

        dataSourceProcessor.setContext(
                DataSourceProcessorContext.builder().fileStorage(fileStorage).build());
        boolean dynamicDataSource = false;

        // parse datasource meta
        DataSourceMeta dataSourceMeta = dataSourceProcessor.parseDataSourceMeta(strDataSourceMeta);
        if (dataSourceMeta != null) {
            dynamicDataSource = dataSourceMeta.dynamicDataSource();
        }

        // create new dataset object
        Dataset dataset =
                constructDataset(datasetId, userInfo, dynamicDataSource, createDatasetRequest);
        // insert dataset to db
        datasetTransactionalWrapper.transactionalAddDataset(
                datasetId, dataset, datasetPermissionList);

        // waiting for upload process
        if (DataSourceType.isUploadDataSource(strDataSourceType)) {
            logger.info("upload data source type: {}, datasetId: {}", strDataSourceType, datasetId);
            return CreateDatasetResponse.builder().datasetId(datasetId).build();
        }

        // dynamic data source
        if (dynamicDataSource) {
            logger.info(
                    "dynamic data source type: {}, datasetId: {}", strDataSourceType, datasetId);
            return CreateDatasetResponse.builder().datasetId(datasetId).build();
        }

        // async process data source
        ThreadPoolUtils.execute(
                executor,
                "DataSourceProcessor.processData",
                datasetId,
                () -> {
                    FileStorageInterface.FilePermissionInfo filePermissionInfo =
                            new FileStorageInterface.FilePermissionInfo();
                    filePermissionInfo.setOwner(userInfo.getUser());
                    DataSourceProcessorContext context =
                            DataSourceProcessorContext.builder()
                                    .dataset(dataset)
                                    .dataSourceMeta(dataSourceMeta)
                                    .datasetConfig(datasetConfig)
                                    .userInfo(userInfo)
                                    .datasetTransactionalWrapper(datasetTransactionalWrapper)
                                    .chunkUpload(chunkUpload)
                                    .fileStorage(fileStorage)
                                    .filePermissionInfo(filePermissionInfo)
                                    .build();

                    try {
                        dataSourceProcessor.setContext(context);
                        dataSourceProcessor.processData(context);
                        dataset.setStatus(DatasetStatus.Success.getCode());
                        dataset.setStatusDesc(DatasetStatus.Success.getMessage());

                        datasetSyncer.syncCreateDataset(userInfo, dataset);
                    } catch (Exception e) {
                        dataset.setStatus(DatasetStatus.Failure.getCode());
                        dataset.setStatusDesc(
                                DatasetStatus.Failure.getMessage() + ":" + e.getMessage());
                    } finally {
                        try {
                            datasetWrapper.updateMeta2DB(dataset);
                        } catch (DatasetException ignore) {
                        }
                    }
                });

        return CreateDatasetResponse.builder().datasetId(datasetId).build();
    }

    /**
     * delete dataset list
     *
     * @param userInfo
     * @param datasetIdList
     * @throws DatasetException
     */
    @Override
    public void deleteDatasetList(UserInfo userInfo, List<String> datasetIdList)
            throws DatasetException {

        if (datasetIdList == null || datasetIdList.isEmpty()) {
            return;
        }

        // sort and distinct
        List<String> newDatasetIdList =
                datasetIdList
                        .stream()
                        .distinct()
                        .sorted(String::compareTo)
                        .collect(Collectors.toList());

        // tx
        List<Dataset> datasetList =
                datasetTransactionalWrapper.transactionalDeleteDatasetList(
                        userInfo, newDatasetIdList);
        // logger.info("datasetIdList: {}", datasetIdList);

        // sync to others
        datasetSyncer.syncDeleteDataset(userInfo, datasetIdList);

        // async remove datasets storage file
        ThreadPoolUtils.execute(
                executor,
                "asyncDeleteDatasetList",
                WeDPRUuidGenerator.generateID(),
                () -> deleteDatasetListStorage(datasetList));
    }

    public void deleteDatasetListStorage(List<Dataset> datasetList) {

        logger.info("delete datasetIdList storage, datasetList: {}", datasetList);

        for (Dataset dataset : datasetList) {
            deleteDatasetStorage(dataset);
        }
    }

    /**
     * delete dataset storage
     *
     * @param dataset
     */
    public void deleteDatasetStorage(Dataset dataset) {

        long startTimeMillis = System.currentTimeMillis();
        String datasetId = dataset.getDatasetId();
        String datasetStorageType = dataset.getDatasetStorageType();
        if (datasetStorageType == null || datasetStorageType.isEmpty()) {
            int status = dataset.getStatus();
            logger.info(
                    "dataset storageType is null, datasetId: {}, storageType: {}, status: {}",
                    datasetId,
                    datasetStorageType,
                    status);
            return;
        }

        String datasetStoragePath = dataset.getDatasetStoragePath();
        logger.info(
                " delete dataset storage begin, datasetId: {}, storageType: {}, storagePath: {}",
                datasetId,
                datasetStorageType,
                datasetStoragePath);

        try {
            StoragePath storagePath =
                    StoragePathBuilder.getInstance(datasetStorageType, datasetStoragePath);
            fileStorage.delete(storagePath);

            long endTimeMillis = System.currentTimeMillis();
            logger.info(
                    " delete dataset storage success, datasetId: {}, storageType: {}, storagePath: {}, cost(ms): {}",
                    datasetId,
                    datasetStorageType,
                    datasetStoragePath,
                    (endTimeMillis - startTimeMillis));
        } catch (Exception e) {
            long endTimeMillis = System.currentTimeMillis();
            logger.error(
                    "delete dataset storage failed, datasetId: {}, storageType: {}, storagePath: {}, cost(ms): {}, e: ",
                    datasetId,
                    datasetStorageType,
                    datasetStoragePath,
                    (endTimeMillis - startTimeMillis),
                    e);
        }
    }

    // CreateDatasetRequest => Dataset
    public Dataset constructDataset(UpdateDatasetRequest updateDatasetRequest) {
        Dataset dataset = new Dataset();

        String datasetVisibilityDetails = updateDatasetRequest.getDatasetVisibilityDetails();
        if (datasetVisibilityDetails == null) {
            datasetVisibilityDetails = "";
        }

        dataset.setDatasetId(updateDatasetRequest.getDatasetId());
        dataset.setDatasetTitle(updateDatasetRequest.getDatasetTitle());
        dataset.setDatasetLabel(updateDatasetRequest.getDatasetLabel());
        dataset.setDatasetDesc(updateDatasetRequest.getDatasetDesc());
        dataset.setVisibility(updateDatasetRequest.getDatasetVisibility());
        dataset.setVisibilityDetails(datasetVisibilityDetails);
        dataset.setApprovalChain(updateDatasetRequest.getApprovalChain());

        return dataset;
    }

    /**
     * update dataset list
     *
     * @param userInfo
     * @param updateDatasetRequestList
     * @throws DatasetException
     */
    @Override
    public void updateDatasetList(
            UserInfo userInfo, List<UpdateDatasetRequest> updateDatasetRequestList)
            throws DatasetException {

        if (updateDatasetRequestList == null || updateDatasetRequestList.isEmpty()) {
            return;
        }

        // sort
        updateDatasetRequestList.sort(Comparator.comparing(UpdateDatasetRequest::getDatasetId));

        List<Dataset> datasetList = new ArrayList<>();
        List<List<DatasetPermission>> datasetPermissionListList = new ArrayList<>();
        for (UpdateDatasetRequest updateDatasetRequest : updateDatasetRequestList) {
            Dataset dataset = constructDataset(updateDatasetRequest);
            datasetList.add(dataset);
            List<DatasetPermission> datasetPermissionList =
                    DatasetPermissionGenerator.generateDatasetVisibilityPermissions(
                            updateDatasetRequest.getDatasetVisibility(),
                            updateDatasetRequest.getDatasetId(),
                            userInfo,
                            updateDatasetRequest.getDatasetVisibilityDetails(),
                            true);
            datasetPermissionListList.add(datasetPermissionList);
        }

        datasetTransactionalWrapper.transactionalUpdateDatasetList(
                userInfo, datasetList, datasetPermissionListList);

        datasetSyncer.syncUpdateDataset(userInfo, datasetList);
    }

    /**
     * query dataset
     *
     * @param userInfo
     * @param datasetId
     * @return
     * @throws DatasetException
     */
    @Override
    public Dataset queryDataset(UserInfo userInfo, String datasetId) throws DatasetException {

        Dataset dataset = datasetMapper.getDatasetByDatasetId(datasetId, false);
        if (dataset == null) {
            logger.error("the dataset does not exist, datasetId: {}", datasetId);
            throw new DatasetException("the dataset does not exist, datasetId: " + datasetId);
        }

        // permission verification
        DatasetUserPermissions datasetUserPermissions =
                DatasetUserPermissionValidator.confirmUserDatasetPermissions(
                        datasetId, userInfo, datasetPermissionMapper, false);
        if (!datasetUserPermissions.isVisible()) {
            logger.info(
                    "user does not have dataset visible permission, user: {}, datasetId: {}",
                    userInfo,
                    datasetId);
            throw new DatasetException(
                    "user does not have dataset visible permission, datasetId: " + datasetId);
        }

        dataset.setPermissions(datasetUserPermissions);

        logger.info("query dataset success, datasetId: {}", datasetId);

        return dataset;
    }

    /**
     * query dataset list
     *
     * @param userInfo
     * @param datasetIdList
     * @return
     * @throws DatasetException
     */
    @Override
    public List<Dataset> queryDatasetList(UserInfo userInfo, List<String> datasetIdList)
            throws DatasetException {

        List<Dataset> datasetList = new ArrayList<>();

        for (String datasetId : datasetIdList) {
            Dataset dataset = queryDataset(userInfo, datasetId);
            datasetList.add(dataset);
        }

        return datasetList;
    }

    /**
     * list dataset by various conditions
     *
     * @param userInfo
     * @param ownerAgency
     * @param ownerUser
     * @param datasetTitle
     * @param permissionType
     * @param startTime
     * @param endTime
     * @param pageNum
     * @param pageSize
     * @return
     * @throws DatasetException
     */
    @Override
    public ListDatasetResponse listDataset(
            UserInfo userInfo,
            String ownerAgency,
            String ownerUser,
            String datasetTitle,
            Integer permissionType,
            String startTime,
            String endTime,
            Integer pageNum,
            Integer pageSize)
            throws DatasetException {

        long startTimeMillis = System.currentTimeMillis();

        logger.info(
                "list dataset begin, ownerAgency: {}, ownerUser: {}, datasetTitle: {}, permissionType: {}, startTime: {}, endTime: {}, pageNum: {}, pageSize: {}",
                ownerAgency,
                ownerUser,
                datasetTitle,
                permissionType,
                startTime,
                endTime,
                pageNum,
                pageSize);

        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }

        // limit pageSize
        if (pageSize == null || pageSize < 0 || pageSize > datasetConfig.getMaxBatchSize()) {
            pageSize = datasetConfig.getMaxBatchSize();
        }

        String user = userInfo.getUser();
        String agency = userInfo.getAgency();
        List<String> userGroupList = userInfo.getUserGroupList();

        String userSubject = DatasetPermissionUtils.toSubjectStr(user, agency);
        List<String> userGroupSubjectList =
                DatasetPermissionUtils.toSubjectStrList(userGroupList, agency);

        try {
            try (Page<Object> objectPage = PageMethod.startPage(pageNum, pageSize)) {

                List<Dataset> datasetList =
                        datasetMapper.queryVisibleDatasetsForUser(
                                user,
                                agency,
                                userSubject,
                                userGroupSubjectList,
                                ownerUser,
                                ownerAgency,
                                datasetTitle,
                                permissionType,
                                startTime,
                                endTime);

                if (!datasetList.isEmpty()) {
                    for (Dataset dataset : datasetList) {
                        String datasetId = dataset.getDatasetId();
                        DatasetUserPermissions datasetUserPermissions =
                                DatasetUserPermissionValidator.confirmUserDatasetPermissions(
                                        datasetId, userInfo, datasetPermissionMapper, false);
                        dataset.setPermissions(datasetUserPermissions);
                    }
                }

                long totalCount = new PageInfo<>(datasetList).getTotal();
                long pageEndOffset = (long) pageNum * pageSize;
                boolean isLast = (pageEndOffset >= totalCount);

                long endTimeMillis = System.currentTimeMillis();

                logger.info(
                        "list dataset end, totalCount: {}, isLast: {}, cost(ms): {}",
                        totalCount,
                        isLast,
                        (endTimeMillis - startTimeMillis));

                return ListDatasetResponse.builder()
                        .totalCount(totalCount)
                        .isLast(isLast)
                        .content(datasetList)
                        .build();
            }
        } catch (Exception e) {
            logger.error("query visible datasets for user db operation exception ,e: ", e);
            throw new DatasetException(
                    "query visible datasets for user db operation exception, " + e.getMessage());
        }
    }

    /**
     * get dataset storage path
     *
     * @param datasetID
     * @return
     * @throws DatasetException
     */
    @Override
    public StoragePath getDatasetStoragePath(String datasetID) throws DatasetException {
        return datasetStoragePathRetriever.getDatasetStoragePath(datasetID);
    }
}
