package com.webank.wedpr.components.scheduler.dag.worker;

import com.webank.wedpr.common.config.WeDPRCommonConfig;
import com.webank.wedpr.common.protocol.ServiceName;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.db.mapper.dataset.mapper.DatasetMapper;
import com.webank.wedpr.components.loadbalancer.LoadBalancer;
import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.scheduler.client.ModelClient;
import com.webank.wedpr.components.scheduler.core.SpdzConnections;
import com.webank.wedpr.components.scheduler.dag.entity.JobWorker;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMetaBuilder;
import com.webank.wedpr.components.scheduler.mapper.JobWorkerMapper;
import com.webank.wedpr.components.storage.api.FileStorageInterface;
import com.webank.wedpr.sdk.jni.transport.model.ServiceMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelWorker extends Worker {

    private static final Logger logger = LoggerFactory.getLogger(ModelWorker.class);

    public ModelWorker(
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
        super(
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

    @Override
    public WorkerStatus onRun() throws Exception {

        String jobId = getJobId();
        String workerId = getWorkerId();
        String args = getArgs();

        // use hash policy to ensure the tasks belong to the same dag execute on the same worker,
        // and can make full use of the cache
        ServiceMeta.EntryPointMeta entryPoint =
                getLoadBalancer()
                        .selectService(
                                LoadBalancer.Policy.HASH,
                                ServiceName.MODEL.getValue(),
                                WeDPRCommonConfig.getWedprZone(),
                                jobId);
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
            String taskId = modelClient.submitTask(args, getJobWorker());
            // poll until the task finished
            return modelClient.pollTask(getJobWorker().getWorkerId());
        } finally {
            long endTimeMillis = System.currentTimeMillis();
            logger.info(
                    "## ml engine run end, jobId: {}, taskId: {}, elapsed: {} ms",
                    jobId,
                    workerId,
                    (endTimeMillis - startTimeMillis));
        }
    }
}
