package com.webank.wedpr.components.dataset.datasource.processor;

import com.webank.wedpr.components.dataset.config.DatasetConfig;
import com.webank.wedpr.components.dataset.datasource.DataSourceMeta;
import com.webank.wedpr.components.dataset.service.ChunkUploadApi;
import com.webank.wedpr.components.db.mapper.dataset.dao.Dataset;
import com.webank.wedpr.components.db.mapper.dataset.dao.UserInfo;
import com.webank.wedpr.components.db.mapper.dataset.mapper.wapper.DatasetTransactionalWrapper;
import com.webank.wedpr.components.storage.api.FileStorageInterface;
import com.webank.wedpr.components.storage.api.StoragePath;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DataSourceProcessorContext {
    // input
    private Dataset dataset;
    private DataSourceMeta dataSourceMeta;
    private ChunkUploadApi chunkUpload;
    private FileStorageInterface fileStorage;
    private FileStorageInterface.FilePermissionInfo filePermissionInfo;
    private DatasetTransactionalWrapper datasetTransactionalWrapper;
    private DatasetConfig datasetConfig;
    private UserInfo userInfo;

    // intermediate state
    private String cvsFilePath;
    private String mergedFilePath;
    private StoragePath storagePath;
}
