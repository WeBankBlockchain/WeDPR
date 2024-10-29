package com.webank.wedpr.components.scheduler.dag.worker;

import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.loadbalancer.EntryPointInfo;
import com.webank.wedpr.components.loadbalancer.LoadBalancer;
import com.webank.wedpr.components.scheduler.client.PsiClient;
import com.webank.wedpr.components.scheduler.dag.entity.JobWorker;
import com.webank.wedpr.components.scheduler.dag.utils.ServiceName;
import com.webank.wedpr.components.scheduler.mapper.JobWorkerMapper;
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
    public void engineRun() throws Exception {

        String jobId = getJobId();
        String workerId = getWorkerId();
        String workerArgs = getArgs();

        EntryPointInfo entryPoint =
                getLoadBalancer()
                        .selectService(LoadBalancer.Policy.ROUND_ROBIN, ServiceName.PSI.getValue());
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
        } finally {
            long endTimeMillis = System.currentTimeMillis();
            logger.info(
                    "## psi engine run end, workerId: {}, elapsed: {} ms",
                    workerId,
                    (endTimeMillis - startTimeMillis));
        }
    }
}
