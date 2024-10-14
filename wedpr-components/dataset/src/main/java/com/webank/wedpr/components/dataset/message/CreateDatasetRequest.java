package com.webank.wedpr.components.dataset.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.webank.wedpr.common.utils.Json2StringDeserializer;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateDatasetRequest {
    // title
    private String datasetTitle;
    // dataset description
    private String datasetDesc;
    // dataset label
    private String datasetLabel;
    // dataset visibility
    private Integer datasetVisibility;
    // dataset visibility description
    @JsonDeserialize(using = Json2StringDeserializer.class)
    private String datasetVisibilityDetails;

    // datasource type
    private String dataSourceType;
    // datasource meta info
    @JsonDeserialize(using = Json2StringDeserializer.class)
    private String dataSourceMeta;

    // approval chain, ["user1", "user2", "user3"]
    @JsonDeserialize(using = Json2StringDeserializer.class)
    private String approvalChain;
}
