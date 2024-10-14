package com.webank.wedpr.components.dataset.controller;

import com.webank.wedpr.common.utils.Common;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.dataset.common.DatasetStatus;
import com.webank.wedpr.components.dataset.config.DatasetConfig;
import com.webank.wedpr.components.dataset.dao.FileChunk;
import com.webank.wedpr.components.dataset.datasource.category.UploadChunkDataSource;
import com.webank.wedpr.components.dataset.datasource.dispatch.DataSourceProcessorDispatcher;
import com.webank.wedpr.components.dataset.datasource.processor.DataSourceProcessor;
import com.webank.wedpr.components.dataset.datasource.processor.DataSourceProcessorContext;
import com.webank.wedpr.components.dataset.message.MergeChunkRequest;
import com.webank.wedpr.components.dataset.service.ChunkUploadApi;
import com.webank.wedpr.components.dataset.sync.api.DatasetSyncerApi;
import com.webank.wedpr.components.dataset.utils.ThreadPoolUtils;
import com.webank.wedpr.components.dataset.utils.UserTokenUtils;
import com.webank.wedpr.components.db.mapper.dataset.common.DatasetConstant;
import com.webank.wedpr.components.db.mapper.dataset.dao.Dataset;
import com.webank.wedpr.components.db.mapper.dataset.dao.UserInfo;
import com.webank.wedpr.components.db.mapper.dataset.datasource.DataSourceType;
import com.webank.wedpr.components.db.mapper.dataset.exception.DatasetException;
import com.webank.wedpr.components.db.mapper.dataset.mapper.DatasetMapper;
import com.webank.wedpr.components.db.mapper.dataset.mapper.wapper.DatasetTransactionalWrapper;
import com.webank.wedpr.components.db.mapper.dataset.mapper.wapper.DatasetWrapper;
import com.webank.wedpr.components.storage.api.FileStorageInterface;
import java.util.concurrent.Executor;
import javax.servlet.http.HttpServletRequest;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// the chunk data upload API

@RestController
@RequestMapping(DatasetConstant.WEDPR_DATASET_API_PREFIX)
@Data
public class ChunkUploadController {

    private static final Logger logger = LoggerFactory.getLogger(ChunkUploadController.class);

    @Autowired private DatasetConfig datasetConfig;

    @Autowired private DatasetMapper datasetMapper;

    @Qualifier("chunkUpload")
    @Autowired
    private ChunkUploadApi chunkUpload;

    @Qualifier("datasetSyncer")
    @Autowired
    DatasetSyncerApi datasetSyncer;

    @Qualifier("fileStorage")
    @Autowired
    private FileStorageInterface fileStorage;

    @Autowired private DataSourceProcessorDispatcher dataSourceProcessorDispatcher;

    @Autowired private DatasetTransactionalWrapper datasetTransactionalWrapper;

    @Autowired private DatasetWrapper datasetWrapper;

    @Autowired
    @Qualifier("datasetAsyncExecutor")
    private Executor executor;

    @PostMapping("uploadChunkData")
    public WeDPRResponse uploadChunkData(
            HttpServletRequest httpServletRequest, FileChunk fileChunk) {

        long startTimeMillis = System.currentTimeMillis();

        logger.info("upload chunk data begin, fileChunk: {}", fileChunk);

        WeDPRResponse weDPRResponse =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);

        String datasetId = fileChunk.getDatasetId();
        String identifier = fileChunk.getIdentifier();
        Integer index = fileChunk.getIndex();
        Integer totalCount = fileChunk.getTotalCount();
        try {

            // UserInfo userInfo = UserTokenUtils.getUserInfo(datasetConfig, httpServletRequest);

            Common.requireNonEmpty("datasetId", datasetId);
            Common.requireNonEmpty("identifier", identifier);
            Common.requireNonNull("index", index);
            Common.requireNonNull("totalCount", totalCount);

            // query the dataset
            Dataset dataset = datasetMapper.getDatasetByDatasetId(datasetId, false);
            if (dataset == null) {
                logger.error("the dataset does not exist, datasetId: {}", datasetId);
                throw new DatasetException("the dataset does not exist, datasetId: " + datasetId);
            }

            // get DataSourceProcessor for the dataset
            String strDataSourceType = dataset.getDataSourceType();
            DataSourceProcessor dataSourceProcessor =
                    dataSourceProcessorDispatcher.getDataSourceProcessor(strDataSourceType);
            if (dataSourceProcessor == null) {
                logger.error(
                        "Unsupported data source type, datasetId: {}, dataSourceType: {}",
                        datasetId,
                        strDataSourceType);
                throw new DatasetException("Unsupported data source type: " + strDataSourceType);
            }

            if (!DataSourceType.isUploadDataSource(strDataSourceType)) {
                logger.error(
                        "the data source does not support chunks upload, datasetId: {}, dataSourceType: {}",
                        datasetId,
                        strDataSourceType);
                throw new DatasetException(
                        "the data source does not support chunks upload, data source type: "
                                + strDataSourceType);
            }

            int status = dataset.getStatus();
            if (status == DatasetStatus.Success.getCode()) {
                logger.error(
                        "the dataset data source have been uploaded, datasetId: {}", datasetId);
                throw new DatasetException(
                        "the dataset data source have been uploaded, datasetId: " + datasetId);
            }

            chunkUpload.uploadChunkData(fileChunk);

            long endTimeMillis = System.currentTimeMillis();
            logger.info(
                    "upload chunk data success, datasetId: {}, fileChunk: {}, cost(ms): {}",
                    datasetId,
                    fileChunk,
                    endTimeMillis - startTimeMillis);
        } catch (DatasetException datasetException) {
            weDPRResponse.setCode(Constant.WEDPR_FAILED);
            weDPRResponse.setMsg(datasetException.getMessage());

            long endTimeMillis = System.currentTimeMillis();
            logger.error(
                    "upload chunk data failed, datasetId: {}, fileChunk: {}, cost(ms): {}, datasetException: ",
                    datasetId,
                    fileChunk,
                    endTimeMillis - startTimeMillis,
                    datasetException);
        } catch (Exception e) {
            weDPRResponse.setCode(Constant.WEDPR_FAILED);
            weDPRResponse.setMsg(e.getMessage());

            long endTimeMillis = System.currentTimeMillis();
            logger.error(
                    "upload chunk data failed, datasetId: {}, fileChunk: {}, cost(ms): {}, e: ",
                    datasetId,
                    fileChunk,
                    endTimeMillis - startTimeMillis,
                    e);
        }

        return weDPRResponse;
    }

    @PostMapping("mergeChunkData")
    public WeDPRResponse mergeChunkData(
            HttpServletRequest httpServletRequest,
            @RequestBody MergeChunkRequest mergeChunkRequest) {

        long startTimeMillis = System.currentTimeMillis();

        logger.info("upload merge data begin, request: {}", mergeChunkRequest);

        WeDPRResponse response =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);

        String datasetId = mergeChunkRequest.getDatasetId();
        String identifier = mergeChunkRequest.getIdentifier();
        Integer totalCount = mergeChunkRequest.getTotalCount();

        try {
            UserInfo userInfo = UserTokenUtils.getUserInfo(datasetConfig, httpServletRequest);

            Common.requireNonEmpty("datasetId", datasetId);
            Common.requireNonEmpty("identifier", identifier);
            Common.requireNonNull("totalCount", totalCount);

            // query the dataset
            Dataset dataset = datasetMapper.getDatasetByDatasetId(datasetId, false);
            if (dataset == null) {
                logger.error("the dataset does not exist, datasetId: {}", datasetId);
                throw new DatasetException("the dataset does not exist, datasetId: " + datasetId);
            }

            int status = dataset.getStatus();
            if (status == DatasetStatus.Success.getCode()) {
                logger.error(
                        "the dataset data source have been uploaded, datasetId: {}", datasetId);
                throw new DatasetException(
                        "the dataset data source have been uploaded, datasetId: " + datasetId);
            }

            // get DataSourceProcessor for the dataset
            String strDataSourceType = dataset.getDataSourceType();
            DataSourceProcessor dataSourceProcessor =
                    dataSourceProcessorDispatcher.getDataSourceProcessor(strDataSourceType);
            if (dataSourceProcessor == null) {
                logger.error(
                        "Unsupported data source type, datasetId: {}, dataSourceType: {}",
                        datasetId,
                        strDataSourceType);
                throw new DatasetException("Unsupported data source type: " + strDataSourceType);
            }

            if (!DataSourceType.isUploadDataSource(strDataSourceType)) {
                logger.error(
                        "the data source does not support chunks upload, datasetId: {}, dataSourceType: {}",
                        datasetId,
                        strDataSourceType);
                throw new DatasetException(
                        "the data source does not support chunks upload, data source type: "
                                + strDataSourceType);
            }

            // transfer mergeChunkRequest to UploadChunkDataSource
            UploadChunkDataSource uploadChunkDataSource = new UploadChunkDataSource();
            uploadChunkDataSource.setDatasetId(datasetId);
            uploadChunkDataSource.setDatasetMD5(mergeChunkRequest.getIdentifier());
            uploadChunkDataSource.setDatasetTotalCount(mergeChunkRequest.getTotalCount());
            uploadChunkDataSource.setDatasetIdentifier(mergeChunkRequest.getIdentifier());

            // async process data source
            ThreadPoolUtils.execute(
                    executor,
                    "DataSourceProcessor.processData",
                    datasetId,
                    () -> {
                        DataSourceProcessorContext context =
                                DataSourceProcessorContext.builder()
                                        .dataset(dataset)
                                        .dataSourceMeta(uploadChunkDataSource)
                                        .datasetConfig(datasetConfig)
                                        .userInfo(userInfo)
                                        .datasetTransactionalWrapper(datasetTransactionalWrapper)
                                        .chunkUpload(chunkUpload)
                                        .fileStorage(fileStorage)
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

            long endTimeMillis = System.currentTimeMillis();
            logger.info(
                    "upload merge data end, datasetId: {}, request: {}, cost(ms): {}",
                    datasetId,
                    mergeChunkRequest,
                    endTimeMillis - startTimeMillis);
        } catch (DatasetException datasetException) {
            response.setCode(Constant.WEDPR_FAILED);
            response.setMsg(datasetException.getMessage());

            long endTimeMillis = System.currentTimeMillis();
            logger.error(
                    "upload merge data failed, datasetId: {}, request: {}, cost(ms): {}, datasetException: ",
                    datasetId,
                    mergeChunkRequest,
                    endTimeMillis - startTimeMillis,
                    datasetException);
        } catch (Exception e) {
            response.setCode(Constant.WEDPR_FAILED);
            response.setMsg(e.getMessage());

            long endTimeMillis = System.currentTimeMillis();
            logger.error(
                    "upload merge data failed, datasetId: {}, request: {}, cost(ms): {}, e: ",
                    datasetId,
                    mergeChunkRequest,
                    endTimeMillis - startTimeMillis,
                    e);
        }

        return response;
    }
}
