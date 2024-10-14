package com.webank.wedpr.components.dataset.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryDatasetListRequest {
    private List<String> datasetIdList;
}
