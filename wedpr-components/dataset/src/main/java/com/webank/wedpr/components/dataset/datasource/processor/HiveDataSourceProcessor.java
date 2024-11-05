package com.webank.wedpr.components.dataset.datasource.processor;

import com.webank.wedpr.common.utils.Common;
import com.webank.wedpr.components.dataset.config.DatasetConfig;
import com.webank.wedpr.components.dataset.config.HiveConfig;
import com.webank.wedpr.components.dataset.datasource.DataSourceMeta;
import com.webank.wedpr.components.dataset.datasource.category.HiveDataSource;
import com.webank.wedpr.components.dataset.sqlutils.SQLUtils;
import com.webank.wedpr.components.dataset.utils.CsvUtils;
import com.webank.wedpr.components.dataset.utils.JsonUtils;
import com.webank.wedpr.components.db.mapper.dataset.dao.Dataset;
import com.webank.wedpr.components.db.mapper.dataset.exception.DatasetException;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HiveDataSourceProcessor extends DBDataSourceProcessor {

    private static final Logger logger = LoggerFactory.getLogger(HiveDataSourceProcessor.class);

    @Override
    public DataSourceMeta parseDataSourceMeta(String strDataSourceMeta) throws DatasetException {
        long startTimeMillis = System.currentTimeMillis();

        HiveDataSource hiveDataSource =
                (HiveDataSource)
                        JsonUtils.jsonString2Object(strDataSourceMeta, HiveDataSource.class);

        String sql = hiveDataSource.getSql();
        Common.requireNonEmpty("sql", sql);
        Boolean dynamicDataSource = hiveDataSource.getDynamicDataSource();
        if (dynamicDataSource != null && dynamicDataSource) {
            hiveDataSource.setDynamicDataSource(true);
        }

        // check if single select
        SQLUtils.isSingleSelectStatement(sql);

        long endTimeMillis = System.currentTimeMillis();
        logger.info(
                " => data source processor stage parse data source meta end, dbDataSource: {}, cost(ms): {}",
                hiveDataSource,
                endTimeMillis - startTimeMillis);

        return hiveDataSource;
    }

    @Override
    public void prepareData() throws DatasetException {
        long startTimeMillis = System.currentTimeMillis();

        DatasetConfig datasetConfig = dataSourceProcessorContext.getDatasetConfig();
        HiveConfig hiveConfig = dataSourceProcessorContext.getHiveConfig();
        HiveDataSource dataSourceMeta =
                (HiveDataSource) dataSourceProcessorContext.getDataSourceMeta();

        Dataset dataset = dataSourceProcessorContext.getDataset();
        String datasetId = dataset.getDatasetId();

        String datasetBaseDir = datasetConfig.getDatasetBaseDir();
        String cvsFilePath = datasetBaseDir + File.separator + datasetId;

        String hiveJdbcUrl = hiveConfig.getHiveJdbcUrl();
        String hiveJdbcUserName = hiveConfig.getHiveUserName();
        String hiveJdbcUserPassword = hiveConfig.getHiveUserPassword();

        CsvUtils.convertDBDataToCsv(
                hiveJdbcUrl,
                hiveJdbcUserName,
                hiveJdbcUserPassword,
                dataSourceMeta.getSql(),
                cvsFilePath);

        dataSourceProcessorContext.setCvsFilePath(cvsFilePath);

        long endTimeMillis = System.currentTimeMillis();
        logger.info(
                " ==> data source processor stage prepare data end, datasetId: {}, cvsFilePath: {}, cost(ms): {}",
                datasetId,
                cvsFilePath,
                endTimeMillis - startTimeMillis);
    }
}
