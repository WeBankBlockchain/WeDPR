package com.webank.wedpr.components.dataset.datasource.category;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.webank.wedpr.components.dataset.datasource.DataSourceMeta;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HdfsDataSource implements DataSourceMeta {
    private String filePath;
}
