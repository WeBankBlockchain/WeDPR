package com.webank.wedpr.components.dataset.datasource.processor;

import com.webank.wedpr.components.dataset.datasource.DBType;
import com.webank.wedpr.components.dataset.datasource.DataSourceMeta;
import com.webank.wedpr.components.dataset.datasource.category.DBDataSource;
import com.webank.wedpr.components.dataset.exception.DatasetException;
import com.webank.wedpr.components.dataset.utils.JsonUtils;
import com.webank.wedpr.core.utils.Common;

public class DBDataSourceProcessor implements DataSourceProcessor {

    @Override
    public boolean isSupportUploadChunkData() {
        return false;
    }

    @Override
    public DataSourceMeta parseDataSourceMeta(String strDataSourceMeta) throws DatasetException {

        long startTimeMillis = System.currentTimeMillis();

        DBDataSource dbDataSource =
                (DBDataSource) JsonUtils.jsonString2Object(strDataSourceMeta, DBDataSource.class);

        String dbType = dbDataSource.getDbType();
        Common.requireNonEmpty("dbType", dbType);
        DBType.isSupportedDBType(dbType);

        String sql = dbDataSource.getSql();
        Common.requireNonEmpty("sql", sql);
        String select = "select";
        if (sql.substring(0, 6).equalsIgnoreCase(select)) {
            logger.error("Only select statement supported, sql: {}", sql);
            throw new DatasetException("Only select statement supported, sql: " + sql);
        }

        // TODO:
        //  1. sql check 防止sql注入等问题

        String dbIp = dbDataSource.getDbIp();
        Common.requireNonEmpty("dbIp", dbIp);
        Integer dbPort = dbDataSource.getDbPort();
        Common.requireNonNull("dbPort", dbPort);
        String database = dbDataSource.getDatabase();
        Common.requireNonEmpty("database", database);
        String userName = dbDataSource.getUserName();
        Common.requireNonEmpty("userName", userName);
        String password = dbDataSource.getPassword();
        Common.requireNonEmpty("password", password);
        // TODO: 密码是加密项，解密获取明文
        //        Boolean dynamicDataSource = dbDataSource.getDynamicDataSource();

        long endTimeMillis = System.currentTimeMillis();
        logger.info(
                " => data source processor stage parse data source meta end, dbDataSource: {}, cost(ms): {}",
                dbDataSource,
                endTimeMillis - startTimeMillis);

        return dbDataSource;
    }

    @Override
    public void prepareData() throws DatasetException {}

    @Override
    public void analyzeData() throws DatasetException {}

    @Override
    public void uploadData2Storage() throws DatasetException {}

    @Override
    public void updateMeta2DB() throws DatasetException {}

    @Override
    public void cleanupData() throws DatasetException {}
}
