package com.webank.wedpr.components.dataset.service;

import com.webank.wedpr.components.dataset.dao.FileChunk;
import com.webank.wedpr.components.dataset.dao.MergeChunkResult;
import com.webank.wedpr.components.dataset.message.MergeChunkRequest;
import com.webank.wedpr.components.db.mapper.dataset.exception.DatasetException;

public interface ChunkUploadApi {
    /**
     * upload chunks data
     *
     * @param fileChunk
     * @throws DatasetException
     */
    void uploadChunkData(FileChunk fileChunk) throws DatasetException;

    /**
     * merge chunks data into single file
     *
     * @param mergeChunkRequest
     * @throws DatasetException
     */
    MergeChunkResult mergeChunkData(MergeChunkRequest mergeChunkRequest) throws DatasetException;

    /**
     * clean up temporary file data
     *
     * @param datasetId
     * @param identifier
     */
    void cleanChunkData(String datasetId, String identifier);
}
