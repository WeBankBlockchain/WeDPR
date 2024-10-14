package com.webank.wedpr.components.dataset.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MergeChunkRequest {

    private String datasetId;

    private String identifier;
    private Integer totalCount;

    private String identifierHash = "MD5";
    private String datasetVersionHash = "SHA-256";
}
