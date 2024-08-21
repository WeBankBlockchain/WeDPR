package com.webank.wedpr.components.dataset.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DatasetStatus {
    Success(0, "Success"),
    Failure(-1, "Failure"),
    Fatal(-2, "Fatal"),
    WaitingForUploadData(1, "WaitingForUploadData"),
    DataUploading(2, "DataUploading"),
    DataAnalyzing(3, "DataAnalyzing"),
    UploadingDataToStorage(4, "UploadingDataToStorage");

    private final Integer code;
    private final String message;
}
