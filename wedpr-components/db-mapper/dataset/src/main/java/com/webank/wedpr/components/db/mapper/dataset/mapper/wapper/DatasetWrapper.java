package com.webank.wedpr.components.db.mapper.dataset.mapper.wapper;

import com.webank.wedpr.components.db.mapper.dataset.common.DatasetCode;
import com.webank.wedpr.components.db.mapper.dataset.dao.Dataset;
import com.webank.wedpr.components.db.mapper.dataset.exception.DatasetException;
import com.webank.wedpr.components.db.mapper.dataset.mapper.DatasetMapper;
import com.webank.wedpr.components.db.mapper.dataset.mapper.DatasetPermissionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DatasetWrapper {

    private static final Logger logger = LoggerFactory.getLogger(DatasetWrapper.class);

    @Autowired private DatasetMapper datasetMapper;
    @Autowired private DatasetPermissionMapper datasetPermissionMapper;

    public void updateDatasetStatus(String datasetId, int code, String msg)
            throws DatasetException {

        try {

            int count = datasetMapper.updateDatasetStatus(datasetId, code, msg);
            if (count != 1) {
                logger.warn(
                        "update dataset status failed???, datasetId: {}, updateCount: {}",
                        datasetId,
                        count);
            } else {
                logger.info(
                        "update dataset status success, datasetId: {}, code: {}, msg :{}, updateCount: {}",
                        datasetId,
                        code,
                        msg,
                        count);
            }

        } catch (Exception e) {
            logger.error(
                    "update dataset status exception, datasetId: {}, code: {}, msg :{}, e: ",
                    datasetId,
                    code,
                    msg,
                    e);
            throw new DatasetException(
                    DatasetCode.DB_ERROR.getCode(), DatasetCode.DB_ERROR.getMessage());
        }
    }

    public void updateDatasetMetaInfo(Dataset dataset) throws DatasetException {
        String datasetId = dataset.getDatasetId();
        try {
            int count = datasetMapper.updateDatasetMetaInfo(dataset);
            if (count != 1) {
                logger.warn(
                        "update dataset meta info failed???, datasetId: {}, dataset: {}, updateCount: {}",
                        datasetId,
                        dataset,
                        count);
            } else {
                logger.info(
                        "update dataset meta info success, datasetId: {}, dataset: {}, updateCount: {}",
                        datasetId,
                        dataset,
                        count);
            }
        } catch (Exception e) {
            logger.error(
                    "update dataset meta info exception, datasetId: {}, dataset: {}, e: ",
                    datasetId,
                    dataset,
                    e);
            throw new DatasetException(
                    DatasetCode.DB_ERROR.getCode(), DatasetCode.DB_ERROR.getMessage());
        }
    }

    public void updateMeta2DB(Dataset dataset) throws DatasetException {
        long startTimeMillis = System.currentTimeMillis();

        String datasetId = dataset.getDatasetId();

        int status = dataset.getStatus();
        String datasetDesc = dataset.getStatusDesc();
        if (status == DatasetCode.SUCCESS.getCode()) {
            updateDatasetMetaInfo(dataset);
        } else {
            updateDatasetStatus(datasetId, status, datasetDesc);
        }

        long endTimeMillis = System.currentTimeMillis();

        logger.info(
                " => update dataset end, datasetId: {}, cost(ms): {}",
                datasetId,
                endTimeMillis - startTimeMillis);
    }
}
