package com.webank.wedpr.components.scheduler.dag.worker;

import com.webank.wedpr.common.protocol.JobType;
import com.webank.wedpr.common.protocol.WorkerNodeType;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.loadbalancer.LoadBalancer;
import com.webank.wedpr.components.scheduler.dag.entity.JobWorker;
import com.webank.wedpr.components.scheduler.mapper.JobWorkerMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkerFactory {

    private static final Logger logger = LoggerFactory.getLogger(WorkerFactory.class);

    public static Worker buildWorker(
            JobWorker jobWorker,
            int workerRetryTimes,
            int workerRetryDelayMillis,
            LoadBalancer loadBalancer,
            JobWorkerMapper jobWorkerMapper)
            throws WeDPRException {

        String jobId = jobWorker.getJobId();
        String workerType = jobWorker.getType();

        if (JobType.isPSIJob(workerType)) {
            return new PsiWorker(
                    jobWorker,
                    workerRetryTimes,
                    workerRetryDelayMillis,
                    loadBalancer,
                    jobWorkerMapper);
        }

        if (WorkerNodeType.isMLJob(workerType)) {
            return new ModelWorker(
                    jobWorker,
                    workerRetryTimes,
                    workerRetryDelayMillis,
                    loadBalancer,
                    jobWorkerMapper);
        }

        if (JobType.isMPCJob(workerType)) {
            return new MpcWorker(
                    jobWorker,
                    workerRetryTimes,
                    workerRetryDelayMillis,
                    loadBalancer,
                    jobWorkerMapper);
        }

        logger.error("Unsupported worker type, jobId: {}, workType: {}", jobId, workerType);

        throw new WeDPRException("Unsupported worker type, workType: " + workerType);
    }
}
