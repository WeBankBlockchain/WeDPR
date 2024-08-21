package com.webank.wedpr.components.dataset.datasource.category;

import com.webank.wedpr.components.dataset.datasource.DataSourceMeta;
import lombok.Data;

@Data
public class UploadChunkDataSource implements DataSourceMeta {

    private String datasetId;
    private String datasetIdentifier;
    private String datasetMD5;
    private Integer datasetTotalCount;
}
