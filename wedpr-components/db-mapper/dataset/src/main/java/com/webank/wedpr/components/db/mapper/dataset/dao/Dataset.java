package com.webank.wedpr.components.db.mapper.dataset.dao;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.webank.wedpr.common.utils.Json2StringDeserializer;
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Dataset */
@TableName("wedpr_dataset")
@ApiModel(value = "WedprDataset对象", description = "数据集记录表")
@Data
public class Dataset {
    private static final Logger logger = LoggerFactory.getLogger(Dataset.class);
    // Note: should chang with the DatasetMapper at the same time
    public static final Integer INVALID_DATASET_STATUS = -100000;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StoragePathMeta {
        private String storageType;
        private String filePath;
    }

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
    @JsonProperty private StoragePathMeta storagePathMeta;

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
    private Integer status = INVALID_DATASET_STATUS;

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

    public void setStatus(Integer status) {
        if (status == null) {
            return;
        }
        this.status = status;
    }

    @SneakyThrows(Exception.class)
    public void setDatasetStoragePath(String datasetStoragePath) {
        this.datasetStoragePath = datasetStoragePath;
        if (StringUtils.isBlank(datasetStoragePath)) {
            return;
        }
        try {
            this.storagePathMeta =
                    ObjectMapperFactory.getObjectMapper()
                            .readValue(datasetStoragePath, StoragePathMeta.class);
        } catch (Exception e) {
            logger.warn(
                    "deserialize datasetStoragePath exception, path: {}, e: ",
                    this.datasetStoragePath,
                    e);
        }
    }
}
