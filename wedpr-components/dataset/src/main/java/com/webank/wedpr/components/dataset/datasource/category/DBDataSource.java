package com.webank.wedpr.components.dataset.datasource.category;

import com.webank.wedpr.components.dataset.datasource.DataSourceMeta;
import lombok.Data;

@Data
public class DBDataSource implements DataSourceMeta {
    // private DBType dbType;
    private String dbType;
    private String dbIp;
    private Integer dbPort;
    private String database;
    private String userName;
    private String password;
    private String sql;
    // Data is loaded once when a data source is created, or on each access
    Boolean dynamicDataSource = false;
}
