package com.webank.wedpr.components.dataset.message;

import lombok.Data;

@Data
public class MergeChunkRequest {

    private String datasetId;

    private String identifier;
    private Integer totalCount;

    private String identifierHash = "MD5";
    private String datasetVersionHash = "SHA-256";
}
