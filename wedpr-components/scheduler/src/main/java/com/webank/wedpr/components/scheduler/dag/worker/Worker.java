package com.webank.wedpr.components.scheduler.dag.worker;

import com.webank.wedpr.components.db.mapper.dataset.mapper.DatasetMapper;
import com.webank.wedpr.components.loadbalancer.LoadBalancer;
import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.scheduler.core.SpdzConnections;
import com.webank.wedpr.components.scheduler.dag.entity.JobWorker;
import com.webank.wedpr.components.scheduler.dag.utils.WorkerUtils;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMetaBuilder;
import com.webank.wedpr.components.scheduler.mapper.JobWorkerMapper;
import com.webank.wedpr.components.storage.api.FileStorageInterface;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public abstract class Worker {

    private static final Logger logger = LoggerFactory.getLogger(Worker.class);

    private final JobWorkerMapper jobWorkerMapper;
    private final DatasetMapper datasetMapper;
    private final LoadBalancer loadBalancer;
    private final FileStorageInterface fileStorageInterface;
    private final FileMetaBuilder fileMetaBuilder;
    private final SpdzConnections spdzConnections;

    private final String jobId;
    private final String workerId;
    private final String workType;
    private final String workStatus;
    private final String args;
    private final JobDO jobDO;
    private final JobWorker jobWorker;

    private int workerRetryTimes = -1;
    private int workerRetryDelayMillis = -1;

    public Worker(
            JobDO jobDO,
            JobWorker jobWorker,
            int workerRetryTimes,
            int workerRetryDelayMillis,
            LoadBalancer loadBalancer,
            JobWorkerMapper jobWorkerMapper,
            DatasetMapper datasetMapper,
            FileStorageInterface fileStorageInterface,
            FileMetaBuilder fileMetaBuilder,
            SpdzConnections spdzConnections) {
        this.jobDO = jobDO;
        this.jobWorker = jobWorker;
        this.jobId = jobWorker.getJobId();
        this.workerId = jobWorker.getWorkerId();
        this.workType = jobWorker.getType();
        this.workStatus = jobWorker.getStatus();
        this.args = jobWorker.getArgs();

        this.workerRetryTimes = workerRetryTimes;
        this.workerRetryDelayMillis = workerRetryDelayMillis;

        this.loadBalancer = loadBalancer;
        this.jobWorkerMapper = jobWorkerMapper;
        this.datasetMapper = datasetMapper;
        this.fileStorageInterface = fileStorageInterface;
        this.fileMetaBuilder = fileMetaBuilder;
        this.spdzConnections = spdzConnections;
    }

    public void logWorker() {
        logger.info(
                " ## view job worker, jobId = {}, workId = {}, workStatus: {}",
                jobId,
                workerId,
                workStatus);
    }

    public void onLaunch() {
        logger.info(workerStartLog(workerId));
    }

    public void onFinished() {
        logger.info(workerEndLog(workerId));
    }

    /**
     * to be impl
     *
     * @return
     */
    public abstract WorkerStatus onRun() throws Exception;

    public WorkerStatus run(String workerStatus) throws Exception {

        if (workerStatus.equals(WorkerStatus.SUCCESS.name())) {
            logger.info(
                    " ## worker has been executed successfully, jobId: {}, workId: {}",
                    jobId,
                    workerId);
            return WorkerStatus.SUCCESS;
        }

        logWorker();
        int retryTimes = this.workerRetryTimes < 0 ? 1 : this.workerRetryTimes;

        int attemptTimes = 0;
        while (attemptTimes++ < retryTimes) {
            try {
                this.onLaunch();
                WorkerStatus status = this.onRun();
                this.onFinished();
                return status;
            } catch (Exception e) {
                if (attemptTimes >= retryTimes) {
                    logger.error(
                            "worker failed after run {} attempts, jobId: {}, workerId: {}",
                            retryTimes,
                            jobId,
                            workerId,
                            e);
                    throw e;
                } else {
                    logger.info(
                            "worker failed and wait for retry, attempts: {}, jobId: {}, workerId: {}",
                            attemptTimes,
                            jobId,
                            workerId,
                            e);
                    WorkerUtils.sleep(workerRetryDelayMillis);
                }
            }
        }

        return WorkerStatus.FAILURE;
    }

    String workerStartLog(String workId) {
        return "=====================start_work_" + workId + "=====================";
    }

    String workerEndLog(String workId) {
        return "=====================end_work_" + workId + "=====================";
    }
}
