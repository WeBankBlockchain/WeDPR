package com.webank.wedpr.components.dataset.message;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateDatasetResponse {
    private String datasetId;
}
