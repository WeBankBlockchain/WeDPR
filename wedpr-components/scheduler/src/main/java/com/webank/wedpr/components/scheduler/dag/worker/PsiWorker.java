package com.webank.wedpr.components.scheduler.dag.worker;

import com.webank.wedpr.common.protocol.ServiceName;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.loadbalancer.LoadBalancer;
import com.webank.wedpr.components.scheduler.client.PsiClient;
import com.webank.wedpr.components.scheduler.dag.entity.JobWorker;
import com.webank.wedpr.components.scheduler.mapper.JobWorkerMapper;
import com.webank.wedpr.sdk.jni.transport.model.ServiceMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PsiWorker extends Worker {

    private static final Logger logger = LoggerFactory.getLogger(PsiWorker.class);

    public PsiWorker(
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
                        .selectService(
                                LoadBalancer.Policy.HASH, ServiceName.PSI.getValue(), null, jobId);
        if (entryPoint == null) {
            logger.error("Unable to find psi service endpoint, jobId: {}", jobId);
            throw new WeDPRException("Unable to find psi service endpoint, jobId: " + jobId);
        }

        long startTimeMillis = System.currentTimeMillis();
        logger.info(
                "## psi engine run begin, endpoint: {}, jobId: {}, taskId: {}, args: {}",
                entryPoint,
                jobId,
                workerId,
                workerArgs);
        String url = entryPoint.getUrl(null);

        if (logger.isDebugEnabled()) {
            logger.debug("psi url: {}, jobId: {}", url, jobId);
        }

        try {
            PsiClient psiClient = new PsiClient(url);
            // submit task
            String taskId = psiClient.submitTask(workerArgs);
            // poll until the task finished
            psiClient.pollTask(taskId);
            return WorkerStatus.SUCCESS;
        } finally {
            long endTimeMillis = System.currentTimeMillis();
            logger.info(
                    "## psi engine run end, jobId: {}, workerId: {}, elapsed: {} ms",
                    jobId,
                    workerId,
                    (endTimeMillis - startTimeMillis));
        }
    }
}
