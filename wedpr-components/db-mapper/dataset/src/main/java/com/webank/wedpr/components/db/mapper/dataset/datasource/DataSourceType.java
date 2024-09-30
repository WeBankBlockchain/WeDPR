package com.webank.wedpr.components.db.mapper.dataset.datasource;

import com.webank.wedpr.components.db.mapper.dataset.exception.DatasetException;

// supported data source types
public enum DataSourceType {
    CSV,
    EXCEL,
    DB,
    HDFS,
    HIVE;

    public static void isValidDataSourceType(String strType) throws DatasetException {
        fromStr(strType);
    }

    public static DataSourceType fromStr(String strType) throws DatasetException {
        DataSourceType[] values = DataSourceType.values();
        for (DataSourceType dataSourceType : values) {
            String name = dataSourceType.name();
            if (name.equalsIgnoreCase(strType)) {
                return dataSourceType;
            }
        }

        throw new DatasetException("Unsupported data source type, type: " + strType);
    }

    public static boolean isUploadDataSource(String strType) throws DatasetException {
        DataSourceType dataSourceType = fromStr(strType);

        return dataSourceType.name().equalsIgnoreCase(CSV.name())
                || dataSourceType.name().equalsIgnoreCase(EXCEL.name());
    }
}
