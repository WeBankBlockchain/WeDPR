package com.webank.wedpr.components.dataset.datasource.processor;

import com.webank.wedpr.components.dataset.datasource.DataSourceMeta;
import com.webank.wedpr.components.db.mapper.dataset.exception.DatasetException;

public class HiveDataSourceProcessor implements DataSourceProcessor {

    @Override
    public DataSourceMeta parseDataSourceMeta(String strDataSourceMeta) throws DatasetException {
        return null;
    }

    @Override
    public void prepareData() throws DatasetException {}

    @Override
    public void analyzeData() throws DatasetException {}

    @Override
    public void uploadData() throws DatasetException {}

    @Override
    public void cleanupData() throws DatasetException {}
}
