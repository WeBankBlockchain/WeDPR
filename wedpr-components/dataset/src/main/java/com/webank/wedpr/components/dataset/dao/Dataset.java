package com.webank.wedpr.components.dataset.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.webank.wedpr.components.dataset.json.Json2StringDeserializer;
import lombok.Data;

/** Dataset */
@Data
public class Dataset {

    private String datasetId;

    private String datasetLabel;

    private String datasetTitle;

    private String datasetDesc;

    private String datasetFields;

    @JsonProperty("datasetHash")
    private String datasetVersionHash;

    private Long datasetSize;

    @JsonProperty("recordCount")
    private Integer datasetRecordCount;

    @JsonProperty("columnCount")
    private Integer datasetColumnCount;

    private String datasetStorageType;

    private String datasetStoragePath;

    // the data source type
    private String dataSourceType;

    // dataset data source parameters, different for each data source, JSON string
    @JsonDeserialize(using = Json2StringDeserializer.class)
    private String dataSourceMeta;

    private String ownerAgencyName;
    private String ownerUserName;

    private int visibility;

    private String visibilityDetails;

    // status, 0：valid
    private int status;

    private String statusDesc;

    // create time
    private String createAt;
    // last update time
    private String updateAt;
    //
    private DatasetUserPermissions permissions;

    public void resetMeta() {
        // datasetFields = "";
        // datasetVersionHash = "";
        /*
        datasetSize = 0L;
        datasetRecordCount = 0;
        datasetColumnCount = 0;
        datasetStorageType = "";
        datasetStoragePath = "";
        */
        // dataSourceType = "";
        dataSourceMeta = "";
        permissions = null;
    }
}
