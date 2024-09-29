package com.webank.wedpr.components.db.mapper.dataset.dao;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.webank.wedpr.core.utils.Json2StringDeserializer;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/** Dataset */
@TableName("wedpr_dataset")
@ApiModel(value = "WedprDataset对象", description = "数据集记录表")
@Data
public class Dataset {

    @TableId("dataset_id")
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

    private String approvalChain;

    // status, 0：valid
    private int status;

    private String statusDesc;

    // create time
    private String createAt;
    // last update time
    private String updateAt;

    @TableField(exist = false)
    private DatasetUserPermissions permissions;

    @TableField(exist = false)
    private Integer count;

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
