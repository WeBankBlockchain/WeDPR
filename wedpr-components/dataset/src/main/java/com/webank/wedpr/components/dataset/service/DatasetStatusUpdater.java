package com.webank.wedpr.components.dataset.service;

import com.webank.wedpr.components.dataset.config.DatasetConfig;
import com.webank.wedpr.components.db.mapper.dataset.mapper.DatasetMapper;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatasetStatusUpdater {

    private static final Logger logger = LoggerFactory.getLogger(DatasetStatusUpdater.class);

    private final DatasetConfig datasetConfig;
    private final DatasetMapper datasetMapper;

    private final ScheduledExecutorService datasetStatusUpdateTimer =
            new ScheduledThreadPoolExecutor(1);

    public DatasetStatusUpdater(DatasetConfig datasetConfig, DatasetMapper datasetMapper) {
        this.datasetConfig = datasetConfig;
        this.datasetMapper = datasetMapper;
    }

    public void start() {
        int datasetStatusUpdateTimerSec = datasetConfig.getDatasetStatusUpdateTimerPeriodSec();
        logger.info(
                "start dataset status update timer, datasetStatusUpdateTimerSec: {}",
                datasetStatusUpdateTimerSec);
        datasetStatusUpdateTimer.scheduleAtFixedRate(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            checkAndUpdateDatasetStatus();
                        } catch (Exception e) {
                            logger.warn("e: ", e);
                        }
                    }
                },
                0,
                datasetConfig.getDatasetStatusUpdateTimerPeriodSec(),
                TimeUnit.SECONDS);

        logger.info("start dataset status update timer success");
    }

    public void checkAndUpdateDatasetStatus() {
        Integer updateIntervalSec = datasetConfig.getDatasetStatusUpdateIntervalSec();
        Integer updateLimitCount = datasetConfig.getDatasetStatusUpdateLimitCount();
        int i = datasetMapper.updateStatusByUpdateInterval(updateIntervalSec, updateLimitCount);
        if (i > 0) {
            logger.info(
                    "update dataset status, updateIntervalSec: {}, updateLimitCount: {}, count: {}",
                    updateIntervalSec,
                    updateLimitCount,
                    i);
        } else {
            logger.debug(
                    "update dataset status, updateIntervalSec: {}, updateLimitCount: {}, count: {}",
                    updateIntervalSec,
                    updateLimitCount,
                    i);
        }
    }
}
