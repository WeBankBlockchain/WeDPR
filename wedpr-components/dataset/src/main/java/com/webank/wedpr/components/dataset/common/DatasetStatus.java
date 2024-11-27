package com.webank.wedpr.components.dataset.common;

import com.webank.wedpr.components.db.mapper.dataset.dao.Dataset;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DatasetStatus {
    Success(0, "Success"), // Note: 0 is the valid status
    Failure(-1, "Failure"), // dataset is in failure and can be processed by retrying
    Fatal(-2, "Fatal"), // dataset is in failure and can not be processed by retrying
    Created(1, "Created"), // dataset is created and waiting for data source processing
    DataAnalyzing(2, "DataAnalyzing"), // data analyzing
    DataUploading(3, "UploadingDataToStorage"), // upload data to storage
    UpdateDatasetMetaState(
            4,
            "UpdateDatasetMetaState"), // update dataset meta info, ie: dataset,column num,data hash
    UploadingChunkData(5, "UploadingChunkData"), // chunk data is being uploaded
    LoadingDBData(6, "LoadingDBData"), // upload database data
    LoadingExcelData(7, "LoadingExcelData"), // loading excel data
    MergingChunkData(8, "MergingChunkData"), // merge chunk data
    InvalidStatus(Dataset.INVALID_DATASET_STATUS, "InvalidStatus");

    private final Integer code;
    private final String message;
}
