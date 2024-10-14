package com.webank.wedpr.components.dataset.datasource.category;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.webank.wedpr.components.dataset.datasource.DataSourceMeta;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadChunkDataSource implements DataSourceMeta {

    private String datasetId;
    private String datasetIdentifier;
    private String datasetMD5;
    private Integer datasetTotalCount;
}
