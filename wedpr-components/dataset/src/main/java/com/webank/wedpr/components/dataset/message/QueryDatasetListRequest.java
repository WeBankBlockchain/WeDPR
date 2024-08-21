package com.webank.wedpr.components.dataset.message;

import java.util.List;
import lombok.Data;

@Data
public class QueryDatasetListRequest {
    private List<String> datasetIdList;
}
