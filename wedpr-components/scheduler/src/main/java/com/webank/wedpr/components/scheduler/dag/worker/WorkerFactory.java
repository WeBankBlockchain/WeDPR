package com.webank.wedpr.components.scheduler.dag.worker;

import com.webank.wedpr.common.protocol.JobType;
import com.webank.wedpr.common.protocol.WorkerNodeType;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.db.mapper.dataset.mapper.DatasetMapper;
import com.webank.wedpr.components.loadbalancer.LoadBalancer;
import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.scheduler.core.SpdzConnections;
import com.webank.wedpr.components.scheduler.dag.entity.JobWorker;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMetaBuilder;
import com.webank.wedpr.components.scheduler.mapper.JobWorkerMapper;
import com.webank.wedpr.components.storage.api.FileStorageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkerFactory {

    private static final Logger logger = LoggerFactory.getLogger(WorkerFactory.class);

    public static Worker buildWorker(
            JobDO jobDO,
            JobWorker jobWorker,
            int workerRetryTimes,
            int workerRetryDelayMillis,
            LoadBalancer loadBalancer,
            JobWorkerMapper jobWorkerMapper,
            DatasetMapper datasetMapper,
            FileStorageInterface fileStorageInterface,
            FileMetaBuilder fileMetaBuilder,
            SpdzConnections spdzConnections)
            throws WeDPRException {

        String jobId = jobWorker.getJobId();
        String workerType = jobWorker.getType();

        if (JobType.isPSIJob(workerType)) {
            return new PsiWorker(
                    jobDO,
                    jobWorker,
                    workerRetryTimes,
                    workerRetryDelayMillis,
                    loadBalancer,
                    jobWorkerMapper,
                    datasetMapper,
                    fileStorageInterface,
                    fileMetaBuilder,
                    spdzConnections);
        }

        if (WorkerNodeType.isMLJob(workerType)) {
            return new ModelWorker(
                    jobDO,
                    jobWorker,
                    workerRetryTimes,
                    workerRetryDelayMillis,
                    loadBalancer,
                    jobWorkerMapper,
                    datasetMapper,
                    fileStorageInterface,
                    fileMetaBuilder,
                    spdzConnections);
        }

        if (JobType.isMPCJob(workerType)) {
            return new MpcWorker(
                    jobDO,
                    jobWorker,
                    workerRetryTimes,
                    workerRetryDelayMillis,
                    loadBalancer,
                    jobWorkerMapper,
                    datasetMapper,
                    fileStorageInterface,
                    fileMetaBuilder,
                    spdzConnections);
        }

        logger.error("Unsupported worker type, jobId: {}, workType: {}", jobId, workerType);

        throw new WeDPRException("Unsupported worker type, workType: " + workerType);
    }
}
