package com.webank.wedpr.components.scheduler.dag.worker;

import com.webank.wedpr.common.protocol.ServiceName;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.loadbalancer.LoadBalancer;
import com.webank.wedpr.components.scheduler.client.MpcClient;
import com.webank.wedpr.components.scheduler.dag.entity.JobWorker;
import com.webank.wedpr.components.scheduler.mapper.JobWorkerMapper;
import com.webank.wedpr.sdk.jni.transport.model.ServiceMeta;
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
    public WorkerStatus engineRun() throws Exception {

        String jobId = getJobId();
        String workerId = getWorkerId();
        String workerArgs = getArgs();

        // // use hash policy to ensure the tasks belong to the same dag execute on the same worker,
        // and can make full use of the cache
        ServiceMeta.EntryPointMeta entryPoint =
                getLoadBalancer()
                        .selectService(LoadBalancer.Policy.HASH, ServiceName.MPC.getValue(), jobId);
        if (entryPoint == null) {
            logger.error("Unable to find mpc service endpoint, jobId: {}", jobId);
            throw new WeDPRException("Unable to find mpc service endpoint, jobId: " + jobId);
        }

        long startTimeMillis = System.currentTimeMillis();
        logger.info(
                "## mpc engine run begin, endpoint: {}, jobId: {}, taskId: {}, args: {}",
                entryPoint,
                jobId,
                workerId,
                workerArgs);

        //        String mpcUrl = MPCExecutorConfig.getMpcUrl();
        String url = entryPoint.getUrl(null);

        if (logger.isDebugEnabled()) {
            logger.debug("mpc url: {}, jobId: {}", url, jobId);
        }

        try {
            MpcClient psiClient = new MpcClient(url);
            // submit task, sync call
            psiClient.submitTask(workerArgs);
        } finally {
            long endTimeMillis = System.currentTimeMillis();
            logger.info(
                    "## mpc engine run end, jobId: {}, workerId: {}, elapsed: {} ms",
                    jobId,
                    workerId,
                    (endTimeMillis - startTimeMillis));
        }

        return WorkerStatus.SUCCESS;
    }
}
