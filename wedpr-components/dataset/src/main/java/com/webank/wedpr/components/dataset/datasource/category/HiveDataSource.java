package com.webank.wedpr.components.dataset.datasource.category;

import com.webank.wedpr.components.dataset.datasource.DataSourceMeta;
import lombok.Data;

@Data
public class HiveDataSource implements DataSourceMeta {
    private String sql;
    // Data is loaded once when a data source is created, or on each access
    boolean dynamicDataSource = false;
}
