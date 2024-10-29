package com.webank.wedpr.components.scheduler.dag.worker;

import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.loadbalancer.EntryPointInfo;
import com.webank.wedpr.components.loadbalancer.LoadBalancer;
import com.webank.wedpr.components.scheduler.client.ModelClient;
import com.webank.wedpr.components.scheduler.dag.entity.JobWorker;
import com.webank.wedpr.components.scheduler.dag.utils.ServiceName;
import com.webank.wedpr.components.scheduler.mapper.JobWorkerMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelWorker extends Worker {

    private static final Logger logger = LoggerFactory.getLogger(ModelWorker.class);

    public ModelWorker(
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
        String args = getArgs();

        EntryPointInfo entryPoint =
                getLoadBalancer()
                        .selectService(
                                LoadBalancer.Policy.ROUND_ROBIN, ServiceName.MODEL.getValue());
        if (entryPoint == null) {
            logger.error("Unable to find ml service endpoint, jobId: {}", jobId);
            throw new WeDPRException("Unable to find ml service endpoint, jobId: " + jobId);
        }

        long startTimeMillis = System.currentTimeMillis();
        String url = entryPoint.getUrl(null);

        if (logger.isDebugEnabled()) {
            logger.debug("model url: {}, jobId: {}", url, jobId);
        }

        try {

            ModelClient modelClient = new ModelClient(url);
            // submit task
            String taskId = modelClient.submitTask(args);
            // poll until the task finished
            modelClient.pollTask(taskId);

        } finally {
            long endTimeMillis = System.currentTimeMillis();
            logger.info(
                    "## psi engine run end, taskId: {}, elapsed: {} ms",
                    workerId,
                    (endTimeMillis - startTimeMillis));
        }
    }
}
