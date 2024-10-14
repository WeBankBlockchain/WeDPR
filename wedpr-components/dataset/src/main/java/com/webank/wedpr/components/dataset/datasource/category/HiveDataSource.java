package com.webank.wedpr.components.dataset.datasource.category;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.webank.wedpr.components.dataset.datasource.DataSourceMeta;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HiveDataSource implements DataSourceMeta {
    private String sql;
    // Data is loaded once when a data source is created, or on each access
    Boolean dynamicDataSource = false;

    @Override
    public boolean dynamicDataSource() {
        return dynamicDataSource;
    }
}
