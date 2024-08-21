package com.webank.wedpr.components.dataset.datasource;

import com.webank.wedpr.components.dataset.exception.DatasetException;

// supported database types
public enum DBType {
    MYSQL,
    DM,
    GAUSS, //
    KING, // KING_BASE,
    SHENTONG,
    POSTFRESQL;
    // NOTE: Add more db type

    public static void isSupportedDBType(String strType) throws DatasetException {
        DBType[] values = DBType.values();
        for (DBType dbType : values) {
            String name = dbType.name();
            if (name.equalsIgnoreCase(strType)) {
                return;
            }
        }

        throw new DatasetException("Unsupported db type, type: " + strType);
    }
}
