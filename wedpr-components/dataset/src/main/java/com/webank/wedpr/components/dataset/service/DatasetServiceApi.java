package com.webank.wedpr.components.dataset.service;

import com.webank.wedpr.components.dataset.message.CreateDatasetRequest;
import com.webank.wedpr.components.dataset.message.CreateDatasetResponse;
import com.webank.wedpr.components.dataset.message.ListDatasetResponse;
import com.webank.wedpr.components.dataset.message.UpdateDatasetRequest;
import com.webank.wedpr.components.db.mapper.dataset.dao.Dataset;
import com.webank.wedpr.components.db.mapper.dataset.dao.UserInfo;
import com.webank.wedpr.components.db.mapper.dataset.exception.DatasetException;
import com.webank.wedpr.components.storage.api.StoragePath;
import java.util.List;

public interface DatasetServiceApi {

    /**
     * create dataset
     *
     * @param userInfo
     * @param createDatasetRequest
     * @return
     * @throws DatasetException
     */
    CreateDatasetResponse createDataset(
            UserInfo userInfo, CreateDatasetRequest createDatasetRequest) throws DatasetException;

    /**
     * delete dataset list
     *
     * @param userInfo
     * @param datasetIdList
     * @throws DatasetException
     */
    void deleteDatasetList(UserInfo userInfo, List<String> datasetIdList) throws DatasetException;

    /**
     * update dataset list
     *
     * @param userInfo
     * @param updateDatasetRequestList
     * @throws DatasetException
     */
    void updateDatasetList(UserInfo userInfo, List<UpdateDatasetRequest> updateDatasetRequestList)
            throws DatasetException;

    /**
     * query dataset by id
     *
     * @param userInfo
     * @param datasetId
     * @return
     * @throws DatasetException
     */
    Dataset queryDataset(UserInfo userInfo, String datasetId) throws DatasetException;

    /**
     * query dataset list by ids
     *
     * @param userInfo
     * @param datasetIdList
     * @return
     * @throws DatasetException
     */
    List<Dataset> queryDatasetList(UserInfo userInfo, List<String> datasetIdList)
            throws DatasetException;

    /**
     * list dataset by various conditions
     *
     * @param userInfo
     * @param ownerAgency
     * @param ownerUser
     * @param datasetTitle
     * @param permissionType
     * @param startTime
     * @param endTime
     * @param pageOffset
     * @param pageSize
     * @return
     * @throws DatasetException
     */
    ListDatasetResponse listDataset(
            UserInfo userInfo,
            String ownerAgency,
            String ownerUser,
            String datasetTitle,
            Integer permissionType,
            String startTime,
            String endTime,
            Integer pageOffset,
            Integer pageSize)
            throws DatasetException;

    /**
     * get dataset storage path
     *
     * @param datasetID
     * @return
     * @throws DatasetException
     */
    StoragePath getDatasetStoragePath(String datasetID) throws DatasetException;
}
