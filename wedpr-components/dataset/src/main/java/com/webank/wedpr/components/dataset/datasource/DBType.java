package com.webank.wedpr.components.dataset.datasource;

import com.webank.wedpr.components.db.mapper.dataset.exception.DatasetException;
import lombok.Getter;

// supported database types
@Getter
public enum DBType {
    // jdbc:mysql://127.0.0.1:3306/[#databaseSchema]?serverTimezone=GMT%2B8&characterEncoding=UTF-8&connectTimeout=60000&socketTimeout=60000
    MYSQL("mysql", "mysql"),
    DM("dm", "dm"), // jdbc:dm://db_ip:db_port?genKeyNameCase=0
    GAUSS("gauss", "postgresql"), // jdbc:postgresql://db_ip:db_port/db_name
    KING("king", "kingbase8"), // KING_BASE, jdbc:kingbase8://db_ip:db_port/db_name
    SHENTONG("shentong", "oscar"), // jdbc:oscar://db_ip:db_port/db_name
    POSTGRESQL("postgresql", "postgresql"); // jdbc:postgresql://db_ip:db_port/db_name
    // NOTE: Add more db type

    private final String type;
    private final String jdbcProtocol;

    DBType(String type, String jdbcProtocol) {
        this.type = type;
        this.jdbcProtocol = jdbcProtocol;
    }

    @Override
    public String toString() {
        return "DBType{" + "type='" + type + '\'' + ", jdbcProtocol='" + jdbcProtocol + '\'' + '}';
    }

    public static DBType fromStrType(String strType) throws DatasetException {
        DBType[] values = DBType.values();
        for (DBType dbType : values) {
            String type = dbType.getType();
            if (type.equalsIgnoreCase(strType)) {
                return dbType;
            }
        }

        throw new DatasetException("Unsupported db type, type: " + strType);
    }
}
