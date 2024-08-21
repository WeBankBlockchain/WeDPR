package com.webank.wedpr.components.dataset.datasource.processor;

import com.webank.wedpr.components.dataset.datasource.DataSourceMeta;
import com.webank.wedpr.components.dataset.exception.DatasetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface DataSourceProcessor {

    static final Logger logger = LoggerFactory.getLogger(DataSourceProcessor.class);

    boolean isSupportUploadChunkData();

    // parse datasource meta
    DataSourceMeta parseDataSourceMeta(String strDataSourceMeta) throws DatasetException;

    default void setContext(DataSourceProcessorContext context) {}

    // prepare data, ie: merge chunk data、convert excel to csv
    void prepareData() throws DatasetException;

    // analyze data
    void analyzeData() throws DatasetException;

    // upload data
    void uploadData2Storage() throws DatasetException;

    // 更新db
    void updateMeta2DB() throws DatasetException;

    // 清理
    void cleanupData() throws DatasetException;

    // 处理数据
    default void processData(DataSourceProcessorContext context) {
        try {
            setContext(context);
            // preprocess data
            // ie: convert data to .cvs format, and other operations
            prepareData();
            // data analysis, reading data fields and data volume
            analyzeData();
            // upload data to storage
            uploadData2Storage();

            context.setSuccess(true);
        } catch (Exception e) {
            context.setSuccess(false);
            context.setErrorMsg(e.getMessage());
            logger.error("process data failed, e: ", e);
        } finally {

            // update dataset metadata or status to the database
            try {
                updateMeta2DB();
            } catch (Exception ignored) {
            }
            // data clean
            try {
                cleanupData();
            } catch (Exception ignored) {

            }
        }
    }
}
