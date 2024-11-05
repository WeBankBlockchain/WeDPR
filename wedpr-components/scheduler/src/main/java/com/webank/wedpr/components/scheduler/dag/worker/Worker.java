package com.webank.wedpr.components.scheduler.dag.worker;

import com.webank.wedpr.components.loadbalancer.LoadBalancer;
import com.webank.wedpr.components.scheduler.dag.entity.JobWorker;
import com.webank.wedpr.components.scheduler.dag.utils.WorkerUtils;
import com.webank.wedpr.components.scheduler.mapper.JobWorkerMapper;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public abstract class Worker {

    private static final Logger logger = LoggerFactory.getLogger(Worker.class);

    private final JobWorkerMapper jobWorkerMapper;
    private final LoadBalancer loadBalancer;

    private final String jobId;
    private final String workerId;
    private final String workType;
    private final String workStatus;
    private final String args;
    private final JobWorker jobWorker;

    private int workerRetryTimes = -1;
    private int workerRetryDelayMillis = -1;

    public Worker(
            JobWorker jobWorker,
            int workerRetryTimes,
            int workerRetryDelayMillis,
            LoadBalancer loadBalancer,
            JobWorkerMapper jobWorkerMapper) {
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
    }

    /*
    public Worker(
            String jobId,
            String workerId,
            String workType,
            String args,
            int workerRetryTimes,
            int workerRetryDelayMillis,
            LoadBalancer loadBalancer,
            JobWorkerMapper jobWorkerMapper) {
        this.jobId = jobId;
        this.workerId = workerId;
        this.workType = workType;
        this.args = args;
        this.workerRetryTimes = workerRetryTimes;
        this.workerRetryDelayMillis = workerRetryDelayMillis;

        this.loadBalancer = loadBalancer;
        this.jobWorkerMapper = jobWorkerMapper;
    }
    */

    public void logWorker() {
        logger.info(
                " ## view job worker, jobId = {}, workId = {}, workStatus: {}",
                jobId,
                workerId,
                workStatus);
    }

    /**
     * to be impl
     *
     * @return
     */
    public abstract WorkerStatus engineRun() throws Exception;

    public WorkerStatus run(String workerStatus) throws Exception {

        if (workerStatus.equals(WorkerStatus.SUCCESS.name())) {
            logger.info(
                    "worker has been executed successfully, jobId: {}, workId: {}",
                    jobId,
                    workerId);
            return WorkerStatus.SUCCESS;
        }

        logWorker();

        int retryTimes = this.workerRetryTimes < 0 ? 1 : this.workerRetryTimes;

        int attemptTimes = 0;
        while (attemptTimes++ < retryTimes) {
            try {
                logger.info(workerStartLog(workerId));
                WorkerStatus status = this.engineRun();
                logger.info(workerEndLog(workerId));
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
