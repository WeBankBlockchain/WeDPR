package com.webank.wedpr.components.dataset.datasource.processor;

import com.webank.wedpr.common.utils.Common;
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.dataset.config.DatasetConfig;
import com.webank.wedpr.components.dataset.datasource.DBType;
import com.webank.wedpr.components.dataset.datasource.DataSourceMeta;
import com.webank.wedpr.components.dataset.datasource.category.DBDataSource;
import com.webank.wedpr.components.dataset.sqlutils.SQLExecutor;
import com.webank.wedpr.components.dataset.sqlutils.SQLUtils;
import com.webank.wedpr.components.dataset.utils.CsvUtils;
import com.webank.wedpr.components.dataset.utils.FileUtils;
import com.webank.wedpr.components.dataset.utils.JsonUtils;
import com.webank.wedpr.components.db.mapper.dataset.dao.Dataset;
import com.webank.wedpr.components.db.mapper.dataset.dao.UserInfo;
import com.webank.wedpr.components.db.mapper.dataset.exception.DatasetException;
import com.webank.wedpr.components.storage.api.FileStorageInterface;
import com.webank.wedpr.components.storage.api.StoragePath;
import com.webank.wedpr.components.user.config.UserJwtConfig;
import com.webank.wedpr.components.user.helper.PasswordHelper;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBDataSourceProcessor extends CsvDataSourceProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DBDataSourceProcessor.class);

    @Override
    public DataSourceMeta parseDataSourceMeta(String strDataSourceMeta, DatasetConfig datasetConfig)
            throws DatasetException {

        long startTimeMillis = System.currentTimeMillis();

        DBDataSource dbDataSource =
                (DBDataSource) JsonUtils.jsonString2Object(strDataSourceMeta, DBDataSource.class);

        String strDbType = dbDataSource.getDbType();
        Common.requireNonEmpty("dbType", strDbType);
        DBType dbType = DBType.fromStrType(strDbType);
        String sql = dbDataSource.getSql();
        Common.requireNonEmpty("sql", sql);
        String dbIp = dbDataSource.getDbIp();
        Common.requireNonEmpty("dbIp", dbIp);
        Integer dbPort = dbDataSource.getDbPort();
        Common.requireNonNull("dbPort", dbPort);
        String database = dbDataSource.getDatabase();
        Common.requireNonEmpty("database", database);
        String encUserName = dbDataSource.getUserName();
        Common.requireNonEmpty("userName", encUserName);

        String encPassword = dbDataSource.getPassword();
        Common.requireNonEmpty("password", encPassword);

        Boolean dynamicDataSource = dbDataSource.getDynamicDataSource();
        if (dynamicDataSource != null && dynamicDataSource) {
            dbDataSource.setDynamicDataSource(true);
        }

        if (dbDataSource.isEncryptionModel()) {
            // decrypt username and password
            try {
                UserJwtConfig userJwtConfig = getDataSourceProcessorContext().getUserJwtConfig();
                String userName =
                        PasswordHelper.decryptPassword(encUserName, userJwtConfig.getPrivateKey());
                dbDataSource.setUserName(userName);

                String password =
                        PasswordHelper.decryptPassword(encPassword, userJwtConfig.getPrivateKey());
                dbDataSource.setPassword(password);
            } catch (WeDPRException e) {
                logger.error("e: ", e);
                throw new DatasetException(e);
            }
        }

        boolean verifySqlSyntaxAndTestCon = dbDataSource.isVerifySqlSyntaxAndTestCon();
        if (verifySqlSyntaxAndTestCon) {
            // check if single select
            SQLUtils.isSingleSelectStatement(sql, datasetConfig.getSqlValidationPattern());
            // validate parameters, test db connectivity, validate SQL syntax
            SQLUtils.validateDataSourceParameters(dbType, dbDataSource);
        }

        long endTimeMillis = System.currentTimeMillis();
        logger.info(
                " => data source processor stage parse data source meta end, dbDataSource: {}, cost(ms): {}",
                dbDataSource,
                endTimeMillis - startTimeMillis);

        return dbDataSource;
    }

    @Override
    public void prepareData() throws DatasetException {
        long startTimeMillis = System.currentTimeMillis();

        DatasetConfig datasetConfig = dataSourceProcessorContext.getDatasetConfig();
        DBDataSource dbDataSource = (DBDataSource) dataSourceProcessorContext.getDataSourceMeta();

        Dataset dataset = dataSourceProcessorContext.getDataset();
        String datasetId = dataset.getDatasetId();

        String strDbType = dbDataSource.getDbType();
        DBType dbType = DBType.fromStrType(strDbType);

        String datasetBaseDir = datasetConfig.getDatasetBaseDir();
        String cvsFilePath = datasetBaseDir + File.separator + datasetId;

        String jdbcUrl =
                SQLExecutor.generateJdbcUrl(
                        dbType,
                        dbDataSource.getDbIp(),
                        dbDataSource.getDbPort(),
                        dbDataSource.getDatabase(),
                        null);

        CsvUtils.convertDBDataToCsv(
                jdbcUrl,
                dbDataSource.getUserName(),
                dbDataSource.getPassword(),
                dbDataSource.getSql(),
                cvsFilePath);

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
        if (rowNum > 1) {
            // Note: minus field header
            rowNum -= 1;
        }
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
        long startTimeMillis = System.currentTimeMillis();

        Dataset dataset = dataSourceProcessorContext.getDataset();
        String datasetId = dataset.getDatasetId();

        String csvFilePath = dataSourceProcessorContext.getCvsFilePath();
        UserInfo userInfo = dataSourceProcessorContext.getUserInfo();
        DataSourceMeta dataSourceMeta = dataSourceProcessorContext.getDataSourceMeta();
        DatasetConfig datasetConfig = dataSourceProcessorContext.getDatasetConfig();

        FileStorageInterface fileStorage = dataSourceProcessorContext.getFileStorage();

        try {
            String userDatasetPath =
                    datasetConfig.getDatasetStoragePath(
                            userInfo.getUser(), datasetId, dataSourceMeta.dynamicDataSource());

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
