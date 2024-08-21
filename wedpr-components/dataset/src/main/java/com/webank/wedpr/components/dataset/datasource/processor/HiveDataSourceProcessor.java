package com.webank.wedpr.components.dataset.datasource.processor;

import com.webank.wedpr.components.dataset.datasource.DataSourceMeta;
import com.webank.wedpr.components.dataset.exception.DatasetException;

public class HiveDataSourceProcessor implements DataSourceProcessor {

    @Override
    public boolean isSupportUploadChunkData() {
        return false;
    }

    @Override
    public DataSourceMeta parseDataSourceMeta(String strDataSourceMeta) throws DatasetException {
        return null;
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
