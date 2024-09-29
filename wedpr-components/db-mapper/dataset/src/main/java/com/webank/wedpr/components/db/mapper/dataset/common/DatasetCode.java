package com.webank.wedpr.components.db.mapper.dataset.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DatasetCode {
    SUCCESS(0, "Success"),
    FAILURE(1, "Internal Error"),

    // TODO: perfect error code
    DB_ERROR(1001, "DB operation failed"),
    DATASET_NOT_EXIST(1002, "The dataset does not exist"),
    FILE_UPLOAD_ERROR(1003, "Upload file failed"),
    UNSUPPORTED_DATA_SOURCE_TYPE(1004, "Unsupported data source type");

    private Integer code;
    private String message;
}
