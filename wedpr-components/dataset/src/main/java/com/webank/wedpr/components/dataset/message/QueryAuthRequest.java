package com.webank.wedpr.components.dataset.message;

import lombok.Data;

@Data
public class QueryAuthRequest {
    private String datasetId;
    private String user;
    private String userGroup;
    private String agency;
}
