package com.webank.wedpr.components.dataset.datasource.category;

import com.webank.wedpr.components.dataset.datasource.DataSourceMeta;
import lombok.Data;

@Data
public class HdfsDataSource implements DataSourceMeta {
    private String filePath;
}
