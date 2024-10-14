package com.webank.wedpr.components.dataset.datasource.processor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.webank.wedpr.common.utils.Common;
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.components.dataset.config.DatasetConfig;
import com.webank.wedpr.components.dataset.dao.MergeChunkResult;
import com.webank.wedpr.components.dataset.datasource.DataSourceMeta;
import com.webank.wedpr.components.dataset.datasource.category.UploadChunkDataSource;
import com.webank.wedpr.components.dataset.message.MergeChunkRequest;
import com.webank.wedpr.components.dataset.service.ChunkUploadApi;
import com.webank.wedpr.components.dataset.utils.CsvUtils;
import com.webank.wedpr.components.dataset.utils.FileUtils;
import com.webank.wedpr.components.dataset.utils.JsonUtils;
import com.webank.wedpr.components.db.mapper.dataset.dao.Dataset;
import com.webank.wedpr.components.db.mapper.dataset.dao.UserInfo;
import com.webank.wedpr.components.db.mapper.dataset.exception.DatasetException;
import com.webank.wedpr.components.storage.api.FileStorageInterface;
import com.webank.wedpr.components.storage.api.StoragePath;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = false)
public class CsvDataSourceProcessor implements DataSourceProcessor {

    private static final Logger logger = LoggerFactory.getLogger(CsvDataSourceProcessor.class);

    protected DataSourceProcessorContext dataSourceProcessorContext;

    @Override
    public DataSourceMeta parseDataSourceMeta(String strDataSourceMeta) throws DatasetException {

        if (strDataSourceMeta == null || strDataSourceMeta.trim().isEmpty()) {
            return null;
        }

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
        String cvsFilePath = dataSourceProcessorContext.getCvsFilePath();
        Dataset dataset = dataSourceProcessorContext.getDataset();

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

        String datasetId = dataset.getDatasetId();

        long endTimeMillis = System.currentTimeMillis();
        logger.info(
                " => data source processor stage analyze data end, datasetId: {}, fieldString: {}, columnNum: {}, rowNum: {}, cost(ms): {}",
                datasetId,
                fieldString,
                columnNum,
                rowNum,
                endTimeMillis - startTimeMillis);
    }

    @Override
    public void uploadData() throws DatasetException {

        long startTimeMillis = System.currentTimeMillis();

        Dataset dataset = dataSourceProcessorContext.getDataset();
        String datasetId = dataset.getDatasetId();

        String csvFilePath = dataSourceProcessorContext.getCvsFilePath();
        UserInfo userInfo = dataSourceProcessorContext.getUserInfo();
        DatasetConfig datasetConfig = dataSourceProcessorContext.getDatasetConfig();

        FileStorageInterface fileStorage = dataSourceProcessorContext.getFileStorage();

        try {
            String userDatasetPath =
                    datasetConfig.getDatasetStoragePath(userInfo.getUser(), datasetId, false);

            StoragePath storagePath =
                    fileStorage.upload(
                            this.dataSourceProcessorContext.getFilePermissionInfo(),
                            true,
                            csvFilePath,
                            userDatasetPath,
                            false);

            String storagePathStr =
                    ObjectMapperFactory.getObjectMapper().writeValueAsString(storagePath);
            this.dataSourceProcessorContext
                    .getDataset()
                    .setDatasetStorageType(fileStorage.type().toString());
            this.dataSourceProcessorContext.getDataset().setDatasetStoragePath(storagePathStr);
            this.dataSourceProcessorContext.setStoragePath(storagePath);

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
                " => data source processor stage upload data end, datasetId: {}, localPath: {}, cost(ms): {}",
                datasetId,
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
}
