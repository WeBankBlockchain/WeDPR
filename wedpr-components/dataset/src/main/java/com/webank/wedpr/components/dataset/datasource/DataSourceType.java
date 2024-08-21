package com.webank.wedpr.components.dataset.datasource;

import com.webank.wedpr.components.dataset.exception.DatasetException;

// supported data source types
public enum DataSourceType {
    CSV,
    EXCEL,
    DB,
    HDFS,
    HIVE;

    public static void isValidDataSourceType(String strType) throws DatasetException {
        DataSourceType[] values = DataSourceType.values();
        for (DataSourceType dataSourceType : values) {
            String name = dataSourceType.name();
            if (name.equalsIgnoreCase(strType)) {
                return;
            }
        }

        throw new DatasetException("Unsupported data source type, type: " + strType);
    }
}
