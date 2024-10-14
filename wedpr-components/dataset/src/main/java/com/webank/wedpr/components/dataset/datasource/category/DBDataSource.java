package com.webank.wedpr.components.dataset.datasource.category;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.webank.wedpr.components.dataset.datasource.DataSourceMeta;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DBDataSource implements DataSourceMeta {
    private String dbType;
    private String dbIp;
    private Integer dbPort;
    private String database;
    private String userName;
    private String password;
    private String sql;
    // Data is loaded once when a data source is created, or on each access
    Boolean dynamicDataSource = false;
    // verify sql syntax and test connectivity
    boolean verifySqlSyntaxAndTestCon = true;

    @Override
    public boolean dynamicDataSource() {
        return dynamicDataSource;
    }
}
