package com.webank.wedpr.components.dataset.datasource.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.webank.wedpr.components.dataset.common.DatasetStatus;
import com.webank.wedpr.components.dataset.config.DatasetConfig;
import com.webank.wedpr.components.dataset.datasource.DataSourceMeta;
import com.webank.wedpr.components.dataset.datasource.dispatch.DataSourceProcessorDispatcher;
import com.webank.wedpr.components.dataset.datasource.processor.DataSourceProcessor;
import com.webank.wedpr.components.dataset.datasource.processor.DataSourceProcessorContext;
import com.webank.wedpr.components.db.mapper.dataset.dao.Dataset;
import com.webank.wedpr.components.db.mapper.dataset.dao.UserInfo;
import com.webank.wedpr.components.db.mapper.dataset.exception.DatasetException;
import com.webank.wedpr.components.db.mapper.dataset.mapper.DatasetMapper;
import com.webank.wedpr.components.db.mapper.dataset.mapper.wapper.DatasetTransactionalWrapper;
import com.webank.wedpr.components.storage.api.FileStorageInterface;
import com.webank.wedpr.components.storage.api.StoragePath;
import com.webank.wedpr.components.storage.builder.StoragePathBuilder;
import com.webank.wedpr.core.utils.WeDPRException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class DatasetStoragePathRetriever {

    private static final Logger logger = LoggerFactory.getLogger(DatasetStoragePathRetriever.class);

    @Autowired private DatasetConfig datasetConfig;
    @Autowired private DatasetMapper datasetMapper;
    @Autowired private DatasetTransactionalWrapper datasetTransactionalWrapper;

    @Qualifier("fileStorage")
    @Autowired
    private FileStorageInterface fileStorage;

    /**
     * get the storage path of the dataset NOTICE: This interface may block for a long time
     *
     * @param datasetID
     * @return
     * @throws DatasetException
     */
    public StoragePath getDatasetStoragePath(String datasetID) throws DatasetException {

        DatasetMapper datasetMapper = datasetTransactionalWrapper.getDatasetMapper();
        Dataset dataset = datasetMapper.getDatasetByDatasetId(datasetID, false);
        if (dataset == null) {
            logger.error("dataset not found, dataset id: {}", datasetID);
            throw new DatasetException("dataset not found, dataset id: " + datasetID);
        }

        return getDatasetStoragePath(dataset);
    }

    /**
     * get the storage path of the dataset NOTICE: This interface may block for a long time
     *
     * @param dataset
     * @return
     * @throws DatasetException
     */
    public StoragePath getDatasetStoragePath(Dataset dataset) throws DatasetException {
        String datasetID = dataset.getDatasetId();
        int status = dataset.getStatus();
        if (status != DatasetStatus.Success.getCode().intValue()) {
            logger.error(
                    "dataset is not available status, dataset id: {}, status: {}",
                    datasetID,
                    status);
            throw new DatasetException(
                    "dataset is not available status, dataset id: "
                            + datasetID
                            + " status: "
                            + status);
        }

        String strDataSourceType = dataset.getDataSourceType();
        String strDataSourceMeta = dataset.getDataSourceMeta();
        String strDatasetStorageType = dataset.getDatasetStorageType();
        String strDatasetStoragePath = dataset.getDatasetStoragePath();

        DataSourceProcessorDispatcher dataSourceProcessorDispatcher =
                new DataSourceProcessorDispatcher();

        DataSourceProcessor dataSourceProcessor =
                dataSourceProcessorDispatcher.getDataSourceProcessor(strDataSourceType);
        DataSourceMeta dataSourceMeta = dataSourceProcessor.parseDataSourceMeta(strDataSourceMeta);

        if ((dataSourceMeta != null) && dataSourceMeta.dynamicDataSource()) {
            // dynamic data source
            return processDynamicDatasourceForStoragePath(
                    dataset, dataSourceMeta, dataSourceProcessor);
        } else {
            return createStoragePath(strDatasetStorageType, strDatasetStoragePath);
        }
    }

    public StoragePath createStoragePath(String strStorageType, String strStoragePath)
            throws DatasetException {
        try {
            return StoragePathBuilder.getInstance(strStorageType, strStoragePath);
        } catch (WeDPRException e) {
            throw new DatasetException(e);
        } catch (JsonProcessingException e) {
            throw new DatasetException(e);
        }
    }

    public StoragePath processDynamicDatasourceForStoragePath(
            Dataset dataset, DataSourceMeta dataSourceMeta, DataSourceProcessor dataSourceProcessor)
            throws DatasetException {

        String ownerUserName = dataset.getOwnerUserName();
        String ownerAgencyName = dataset.getOwnerAgencyName();

        UserInfo userInfo = UserInfo.builder().user(ownerUserName).agency(ownerAgencyName).build();

        DataSourceProcessorContext context =
                DataSourceProcessorContext.builder()
                        .dataset(dataset)
                        .dataSourceMeta(dataSourceMeta)
                        .datasetConfig(datasetConfig)
                        .userInfo(userInfo)
                        .datasetTransactionalWrapper(datasetTransactionalWrapper)
                        .fileStorage(fileStorage)
                        .build();

        try {
            dataSourceProcessor.setContext(context);
            dataSourceProcessor.processData(context);
            StoragePath storagePath = context.getStoragePath();

            logger.info(
                    "process dynamic data source success, dataset id: {}, datasource type: {}, datasource meta: {}, storage path: {}",
                    dataset.getDatasetId(),
                    dataset.getDataSourceType(),
                    dataset.getDataSourceMeta(),
                    storagePath);

            return storagePath;
        } catch (Exception e) {
            logger.error(
                    "process dynamic data source exception, dataset id: {}, datasource type: {}, datasource meta: {}, e: ",
                    dataset.getDatasetId(),
                    dataset.getDataSourceType(),
                    dataset.getDataSourceMeta(),
                    e);
            throw e;
        }
    }
}
