package com.webank.wedpr.components.dataset.datasource.processor;

import com.webank.wedpr.common.protocol.StorageType;
import com.webank.wedpr.common.utils.Common;
import com.webank.wedpr.components.dataset.config.DatasetConfig;
import com.webank.wedpr.components.dataset.datasource.DataSourceMeta;
import com.webank.wedpr.components.dataset.datasource.category.HdfsDataSource;
import com.webank.wedpr.components.dataset.utils.CsvUtils;
import com.webank.wedpr.components.dataset.utils.FileUtils;
import com.webank.wedpr.components.dataset.utils.JsonUtils;
import com.webank.wedpr.components.db.mapper.dataset.dao.Dataset;
import com.webank.wedpr.components.db.mapper.dataset.exception.DatasetException;
import com.webank.wedpr.components.storage.api.FileStorageInterface;
import com.webank.wedpr.components.storage.impl.hdfs.HDFSStoragePath;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HdfsDataSourceProcessor implements DataSourceProcessor {

    private static final Logger logger = LoggerFactory.getLogger(HdfsDataSourceProcessor.class);

    protected DataSourceProcessorContext dataSourceProcessorContext;

    @Override
    public void setContext(DataSourceProcessorContext context) {
        this.dataSourceProcessorContext = context;
    }

    @Override
    public DataSourceMeta parseDataSourceMeta(String strDataSourceMeta) throws DatasetException {
        long startTimeMillis = System.currentTimeMillis();

        HdfsDataSource hdfsDataSource =
                (HdfsDataSource)
                        JsonUtils.jsonString2Object(strDataSourceMeta, HdfsDataSource.class);

        String filePath = hdfsDataSource.getFilePath();
        Common.requireNonEmpty("filePath", filePath);

        FileStorageInterface fileStorage = dataSourceProcessorContext.getFileStorage();

        StorageType storageType = fileStorage.type();
        if (!storageType.getName().equalsIgnoreCase(StorageType.HDFS.getName())) {
            // NOT HDFS Storage
            logger.error("Not supported for HDFS data source, type: {}", storageType);
            throw new DatasetException(
                    "Not supported for HDFS data source, type: " + storageType.getName());
        }

        checkFileExists(fileStorage, filePath);

        long endTimeMillis = System.currentTimeMillis();
        logger.info(
                " => data source processor stage parse data source meta end, datasetId: {}, hdfsDataSource: {}, cost(ms): {}",
                filePath,
                hdfsDataSource,
                endTimeMillis - startTimeMillis);

        return hdfsDataSource;
    }

    public void checkFileExists(FileStorageInterface storageInterface, String filePath)
            throws DatasetException {
        HDFSStoragePath hdfsStoragePath = new HDFSStoragePath(filePath);
        boolean exists = storageInterface.exists(hdfsStoragePath);
        if (!exists) {
            logger.error("HDFS file does not exist, filePath: {}", filePath);
            throw new DatasetException("HDFS file does not exist, filePath: " + filePath);
        }
    }

    @Override
    public void prepareData() throws DatasetException {
        long startTimeMillis = System.currentTimeMillis();

        DatasetConfig datasetConfig = dataSourceProcessorContext.getDatasetConfig();
        FileStorageInterface fileStorage = dataSourceProcessorContext.getFileStorage();
        HdfsDataSource hdfsDataSource =
                (HdfsDataSource) dataSourceProcessorContext.getDataSourceMeta();

        Dataset dataset = dataSourceProcessorContext.getDataset();
        String datasetId = dataset.getDatasetId();

        String datasetBaseDir = datasetConfig.getDatasetBaseDir();
        String cvsFilePath = datasetBaseDir + File.separator + datasetId;

        String filePath = hdfsDataSource.getFilePath();
        HDFSStoragePath hdfsStoragePath = new HDFSStoragePath(filePath);
        fileStorage.download(hdfsStoragePath, cvsFilePath);

        dataSourceProcessorContext.setCvsFilePath(cvsFilePath);

        long endTimeMillis = System.currentTimeMillis();
        logger.info(
                " ==> data source processor stage prepare data end, datasetId: {}, cvsFilePath: {}, cost(ms): {}",
                datasetId,
                cvsFilePath,
                endTimeMillis - startTimeMillis);
    }

    @Override
    public void analyzeData() throws DatasetException {
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
        String md5Hash = FileUtils.calculateFileHash(cvsFilePath, "MD5");
        long fileSize = FileUtils.getFileSize(cvsFilePath);

        this.dataSourceProcessorContext.getDataset().setDatasetFields(fieldString);
        this.dataSourceProcessorContext.getDataset().setDatasetColumnCount(columnNum);
        this.dataSourceProcessorContext.getDataset().setDatasetRecordCount(rowNum);
        this.dataSourceProcessorContext.getDataset().setDatasetVersionHash(md5Hash);
        this.dataSourceProcessorContext.getDataset().setDatasetSize(fileSize);

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
        // do nothing
    }

    @Override
    public void cleanupData() throws DatasetException {
        long startTimeMillis = System.currentTimeMillis();
        DatasetConfig datasetConfig = dataSourceProcessorContext.getDatasetConfig();
        Dataset dataset = dataSourceProcessorContext.getDataset();

        String datasetId = dataset.getDatasetId();
        String datasetBaseDir = datasetConfig.getDatasetBaseDir();
        String cvsFilePath = datasetBaseDir + File.separator + datasetId;
        try {
            FileUtils.deleteDirectory(new File(cvsFilePath));
            logger.info(
                    "remove temp csv success, datasetId: {}, cvsFilePath: {}",
                    datasetId,
                    cvsFilePath);
        } catch (Exception e) {
            logger.warn(
                    "remove temp csv failed, datasetId: {}, cvsFilePath: {}",
                    datasetId,
                    cvsFilePath);
        }

        long endTimeMillis = System.currentTimeMillis();
        logger.info(
                " => data source processor stage cleanup data end, datasetId: {}, cost(ms): {}",
                datasetId,
                endTimeMillis - startTimeMillis);
    }
}
