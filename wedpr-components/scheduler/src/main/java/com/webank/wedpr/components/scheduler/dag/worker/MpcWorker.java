package com.webank.wedpr.components.scheduler.dag.worker;

import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.loadbalancer.EntryPointInfo;
import com.webank.wedpr.components.loadbalancer.LoadBalancer;
import com.webank.wedpr.components.scheduler.dag.entity.JobWorker;
import com.webank.wedpr.components.scheduler.dag.utils.ServiceName;
import com.webank.wedpr.components.scheduler.mapper.JobWorkerMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MpcWorker extends Worker {

    private static final Logger logger = LoggerFactory.getLogger(MpcWorker.class);

    public MpcWorker(
            JobWorker jobWorker,
            int workerRetryTimes,
            int workerRetryDelayMillis,
            LoadBalancer loadBalancer,
            JobWorkerMapper jobWorkerMapper) {
        super(jobWorker, workerRetryTimes, workerRetryDelayMillis, loadBalancer, jobWorkerMapper);
    }

    @Override
    public WorkerStatus engineRun() throws WeDPRException {

        EntryPointInfo entryPoint =
                getLoadBalancer()
                        .selectService(LoadBalancer.Policy.ROUND_ROBIN, ServiceName.MPC.getValue());
        if (entryPoint == null) {
            throw new WeDPRException("cannot find mpc client endpoint, jobId: " + getJobId());
        }

        logger.info("## getting mpc client: {}", entryPoint);
        return WorkerStatus.SUCCESS;
    }
}
