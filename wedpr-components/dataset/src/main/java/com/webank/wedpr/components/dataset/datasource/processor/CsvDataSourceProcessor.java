package com.webank.wedpr.components.dataset.datasource.processor;

import com.webank.wedpr.components.dataset.common.DatasetCode;
import com.webank.wedpr.components.dataset.common.DatasetStatus;
import com.webank.wedpr.components.dataset.config.DatasetConfig;
import com.webank.wedpr.components.dataset.dao.Dataset;
import com.webank.wedpr.components.dataset.dao.MergeChunkResult;
import com.webank.wedpr.components.dataset.dao.UserInfo;
import com.webank.wedpr.components.dataset.datasource.DataSourceMeta;
import com.webank.wedpr.components.dataset.datasource.category.UploadChunkDataSource;
import com.webank.wedpr.components.dataset.exception.DatasetException;
import com.webank.wedpr.components.dataset.mapper.DatasetMapper;
import com.webank.wedpr.components.dataset.message.MergeChunkRequest;
import com.webank.wedpr.components.dataset.service.ChunkUploadApi;
import com.webank.wedpr.components.dataset.utils.CsvUtils;
import com.webank.wedpr.components.dataset.utils.FileUtils;
import com.webank.wedpr.components.dataset.utils.JsonUtils;
import com.webank.wedpr.components.storage.api.FileStorageInterface;
import com.webank.wedpr.components.storage.api.StoragePath;
import com.webank.wedpr.core.config.WeDPRCommonConfig;
import com.webank.wedpr.core.utils.Common;
import com.webank.wedpr.core.utils.ObjectMapperFactory;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
@EqualsAndHashCode(callSuper = false)
public class CsvDataSourceProcessor implements DataSourceProcessor {

    private static final Logger logger = LoggerFactory.getLogger(CsvDataSourceProcessor.class);

    protected DataSourceProcessorContext dataSourceProcessorContext;

    @Override
    public boolean isSupportUploadChunkData() {
        return true;
    }

    @Override
    public DataSourceMeta parseDataSourceMeta(String strDataSourceMeta) throws DatasetException {

        long startTimeMillis = System.currentTimeMillis();
        UploadChunkDataSource uploadChunkDataSource =
                (UploadChunkDataSource)
                        JsonUtils.jsonString2Object(strDataSourceMeta, UploadChunkDataSource.class);

        String datasetId = uploadChunkDataSource.getDatasetId();
        Common.requireNonEmpty("datasetId", datasetId);
        String datasetMD5 = uploadChunkDataSource.getDatasetMD5();
        Common.requireNonEmpty("datasetMD5", datasetMD5);
        String datasetIdentifier = uploadChunkDataSource.getDatasetIdentifier();
        Common.requireNonEmpty("datasetIdentifier", datasetIdentifier);
        Integer datasetTotalCount = uploadChunkDataSource.getDatasetTotalCount();
        Common.requireNonNull("datasetTotalCount", datasetTotalCount);

        long endTimeMillis = System.currentTimeMillis();
        logger.info(
                " => data source processor stage parse data source meta end, datasetId: {}, uploadChunkDataSource: {}, cost(ms): {}",
                datasetId,
                uploadChunkDataSource,
                endTimeMillis - startTimeMillis);

        return uploadChunkDataSource;
    }

    @Override
    public void setContext(DataSourceProcessorContext context) {
        this.dataSourceProcessorContext = context;
    }

    @Override
    public void prepareData() throws DatasetException {

        long startTimeMillis = System.currentTimeMillis();

        UploadChunkDataSource uploadChunkDataSource =
                (UploadChunkDataSource) dataSourceProcessorContext.getDataSourceMeta();
        ChunkUploadApi chunkUpload = dataSourceProcessorContext.getChunkUpload();
        DatasetConfig datasetConfig = dataSourceProcessorContext.getDatasetConfig();
        String datasetHash = datasetConfig.getDatasetHash();

        // merge chunk data
        String datasetId = uploadChunkDataSource.getDatasetId();
        String datasetIdentifier = uploadChunkDataSource.getDatasetIdentifier();
        int datasetTotalCount = uploadChunkDataSource.getDatasetTotalCount();
        String datasetMD5 = uploadChunkDataSource.getDatasetMD5();

        MergeChunkRequest mergeChunkRequest = new MergeChunkRequest();
        mergeChunkRequest.setDatasetId(datasetId);
        mergeChunkRequest.setTotalCount(datasetTotalCount);
        mergeChunkRequest.setIdentifier(datasetIdentifier);
        mergeChunkRequest.setDatasetVersionHash(datasetHash);

        MergeChunkResult mergeChunkResult = chunkUpload.mergeChunkData(mergeChunkRequest);

        String tempMergedFilePath = mergeChunkResult.getMergedFilePath();
        long datasetSize = mergeChunkResult.getDatasetSize();
        String datasetVersionHash = mergeChunkResult.getDatasetVersionHash();

        this.dataSourceProcessorContext.getDataset().setDatasetSize(datasetSize);
        this.dataSourceProcessorContext.getDataset().setDatasetVersionHash(datasetVersionHash);
        this.dataSourceProcessorContext.setMergedFilePath(tempMergedFilePath);
        this.dataSourceProcessorContext.setCvsFilePath(tempMergedFilePath);

        long endTimeMillis = System.currentTimeMillis();

        logger.info(
                " => data source processor stage prepare data end merge chunk data, datasetId: {}, datasetSize: {}, datasetVersionHash: {}, datasetMD5: {}, mergedFilePath: {}, cost(ms): {}",
                datasetId,
                datasetSize,
                datasetVersionHash,
                datasetMD5,
                tempMergedFilePath,
                endTimeMillis - startTimeMillis);
    }

    @Override
    public void analyzeData() throws DatasetException {
        // analyze csv file

        UploadChunkDataSource uploadChunkDataSource =
                (UploadChunkDataSource) dataSourceProcessorContext.getDataSourceMeta();

        String cvsFilePath = dataSourceProcessorContext.getCvsFilePath();

        long startTimeMillis = System.currentTimeMillis();

        // read csv header field
        List<String> fieldList = CsvUtils.readCsvHeader(cvsFilePath);

        // [ x, y ,z] => x,y,z
        String fieldListString = Arrays.toString(fieldList.toArray());
        String fieldString =
                fieldListString
                        .replace("'", "")
                        .replace("\\r", "")
                        .replace("[", "")
                        .replace("]", "")
                        .trim();

        int columnNum = fieldList.size();
        int rowNum = FileUtils.getFileLinesNumber(cvsFilePath);

        this.dataSourceProcessorContext.getDataset().setDatasetFields(fieldString);
        this.dataSourceProcessorContext.getDataset().setDatasetColumnCount(columnNum);
        this.dataSourceProcessorContext.getDataset().setDatasetRecordCount(rowNum);

        String datasetId = uploadChunkDataSource.getDatasetId();
        String identifier = uploadChunkDataSource.getDatasetIdentifier();

        long endTimeMillis = System.currentTimeMillis();
        logger.info(
                " => data source processor stage analyze data end, datasetId: {}, identifier: {}, fieldString: {}, columnNum: {}, rowNum: {}, cost(ms): {}",
                datasetId,
                fieldString,
                identifier,
                columnNum,
                rowNum,
                endTimeMillis - startTimeMillis);
    }

    @Override
    public void uploadData2Storage() throws DatasetException {

        long startTimeMillis = System.currentTimeMillis();

        UploadChunkDataSource uploadChunkDataSource =
                (UploadChunkDataSource) dataSourceProcessorContext.getDataSourceMeta();

        String datasetId = uploadChunkDataSource.getDatasetId();
        String datasetIdentifier = uploadChunkDataSource.getDatasetIdentifier();

        String csvFilePath = dataSourceProcessorContext.getCvsFilePath();
        UserInfo userInfo = dataSourceProcessorContext.getUserInfo();

        FileStorageInterface fileStorage = dataSourceProcessorContext.getFileStorage();

        try {
            String userDatasetPath =
                    WeDPRCommonConfig.getUserDatasetPath(userInfo.getUser(), datasetId);

            StoragePath storagePath = fileStorage.upload(true, csvFilePath, userDatasetPath, false);

            String storagePathStr =
                    ObjectMapperFactory.getObjectMapper().writeValueAsString(storagePath);
            this.dataSourceProcessorContext
                    .getDataset()
                    .setDatasetStorageType(fileStorage.type().toString());
            this.dataSourceProcessorContext.getDataset().setDatasetStoragePath(storagePathStr);

            long endTimeMillis = System.currentTimeMillis();
            logger.info(
                    "upload file to storage success, datasetId: {}, localPath: {}, storagePath: {}, cost(ms): {}",
                    datasetId,
                    csvFilePath,
                    storagePathStr,
                    endTimeMillis - startTimeMillis);
        } catch (Exception e) {
            long endTimeMillis = System.currentTimeMillis();
            logger.error(
                    "upload file to storage exception, localPath: {}, cost(ms): {}, e: ",
                    csvFilePath,
                    endTimeMillis - startTimeMillis,
                    e);

            throw new DatasetException("Upload file failed, e: " + e.getMessage());
        }

        long endTimeMillis = System.currentTimeMillis();
        logger.info(
                " => data source processor stage upload data end, datasetId: {}, identifier: {}, localPath: {}, cost(ms): {}",
                datasetId,
                datasetIdentifier,
                csvFilePath,
                endTimeMillis - startTimeMillis);
    }

    @Override
    public void cleanupData() throws DatasetException {

        long startTimeMillis = System.currentTimeMillis();

        UploadChunkDataSource uploadChunkDataSource =
                (UploadChunkDataSource) dataSourceProcessorContext.getDataSourceMeta();
        ChunkUploadApi chunkUpload = dataSourceProcessorContext.getChunkUpload();

        String datasetId = uploadChunkDataSource.getDatasetId();
        String datasetIdentifier = uploadChunkDataSource.getDatasetIdentifier();

        chunkUpload.cleanChunkData(datasetId, datasetIdentifier);

        long endTimeMillis = System.currentTimeMillis();
        logger.info(
                " => data source processor stage cleanup data end, datasetId: {}, identifier: {}, cost(ms): {}",
                datasetId,
                datasetIdentifier,
                endTimeMillis - startTimeMillis);
    }

    @Override
    public void updateMeta2DB() throws DatasetException {
        long startTimeMillis = System.currentTimeMillis();

        Dataset dataset = dataSourceProcessorContext.getDataset();
        DatasetMapper datasetMapper =
                dataSourceProcessorContext.getDatasetTransactionalWrapper().getDatasetMapper();

        String datasetId = dataset.getDatasetId();
        boolean success = dataSourceProcessorContext.isSuccess();
        String errorMsg = dataSourceProcessorContext.getErrorMsg();

        int updateCount = 0;
        try {
            if (success) {
                dataset.setStatus(DatasetStatus.Success.getCode());
                dataset.setStatusDesc(DatasetStatus.Success.getMessage());
                updateCount = datasetMapper.updateDatasetMetaInfo(dataset);
            } else {
                updateCount =
                        datasetMapper.updateDatasetStatus(
                                datasetId,
                                DatasetStatus.Failure.getCode(),
                                DatasetStatus.Failure.getMessage() + ":" + errorMsg);
            }

            if (updateCount != 1) {
                // update failed ??
                logger.warn(
                        "update dataset meta info failed, datasetId: {}, updateCount: {}, success: {}",
                        datasetId,
                        updateCount,
                        success);
            } else {
                logger.info(
                        "update dataset meta info success, datasetId: {}, updateCount: {}, success: {}",
                        datasetId,
                        updateCount,
                        success);
            }
        } catch (Exception e) {
            logger.error("update dataset meta info exception, datasetId: {}, e: ", datasetId, e);
            throw new DatasetException(
                    DatasetCode.DB_ERROR.getCode(), DatasetCode.DB_ERROR.getMessage());
        }

        long endTimeMillis = System.currentTimeMillis();

        logger.info(
                " => data source processor stage update db meta end, datasetId: {}, cost(ms): {}",
                datasetId,
                endTimeMillis - startTimeMillis);
    }
}
