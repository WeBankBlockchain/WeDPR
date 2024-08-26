package com.webank.wedpr.components.dataset.message;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.webank.wedpr.components.dataset.json.Json2StringDeserializer;
import lombok.Data;

@Data
public class UpdateDatasetRequest {

    // datasetId
    private String datasetId;

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

    // approval chain, ["user1", "user2", "user3"]
    @JsonDeserialize(using = Json2StringDeserializer.class)
    private String approvalChain;
}
