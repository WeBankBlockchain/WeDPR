package com.webank.wedpr.components.scheduler.dag.worker;

import com.webank.wedpr.common.config.WeDPRCommonConfig;
import com.webank.wedpr.common.protocol.ServiceName;
import com.webank.wedpr.common.utils.Common;
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.db.mapper.dataset.mapper.DatasetMapper;
import com.webank.wedpr.components.loadbalancer.LoadBalancer;
import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.scheduler.client.MpcClient;
import com.webank.wedpr.components.scheduler.dag.entity.JobWorker;
import com.webank.wedpr.components.scheduler.dag.utils.MpcResultFileResolver;
import com.webank.wedpr.components.scheduler.executor.hook.MPCExecutorHook;
import com.webank.wedpr.components.scheduler.executor.impl.ExecutorConfig;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMetaBuilder;
import com.webank.wedpr.components.scheduler.executor.impl.mpc.MPCJobParam;
import com.webank.wedpr.components.scheduler.executor.impl.mpc.request.MpcRunJobRequest;
import com.webank.wedpr.components.scheduler.mapper.JobWorkerMapper;
import com.webank.wedpr.components.storage.api.FileStorageInterface;
import com.webank.wedpr.components.storage.impl.hdfs.HDFSStoragePath;
import com.webank.wedpr.sdk.jni.transport.model.ServiceMeta;
import java.io.File;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MpcWorker extends Worker {

    private static final Logger logger = LoggerFactory.getLogger(MpcWorker.class);

    public MpcWorker(
            JobDO jobDO,
            JobWorker jobWorker,
            int workerRetryTimes,
            int workerRetryDelayMillis,
            LoadBalancer loadBalancer,
            JobWorkerMapper jobWorkerMapper,
            DatasetMapper datasetMapper,
            FileStorageInterface fileStorageInterface,
            FileMetaBuilder fileMetaBuilder) {
        super(
                jobDO,
                jobWorker,
                workerRetryTimes,
                workerRetryDelayMillis,
                loadBalancer,
                jobWorkerMapper,
                datasetMapper,
                fileStorageInterface,
                fileMetaBuilder);
    }

    @SneakyThrows
    @Override
    public void onLaunch() {
        super.onLaunch();

        long startTimeMillis = System.currentTimeMillis();
        JobDO jobDO = getJobDO();
        MPCJobParam mpcJobParam = (MPCJobParam) getJobDO().getJobParam();

        logger.info(
                "## mpc engine launch begin, jobId: {}, mpcJobParams: {}",
                jobDO.getId(),
                mpcJobParam);

        MPCExecutorHook mpcExecutorHook =
                new MPCExecutorHook(
                        getDatasetMapper(), getFileStorageInterface(), getFileMetaBuilder());
        boolean needRunPsi = mpcJobParam.isNeedRunPsi();
        if (needRunPsi) {
            mpcExecutorHook.prepareWithPsi(getDatasetMapper(), jobDO, mpcJobParam);
        } else {
            mpcExecutorHook.prepareWithoutPsi(getDatasetMapper(), jobDO, mpcJobParam);
        }

        long endTimeMillis = System.currentTimeMillis();
        logger.info(
                "## mpc engine launch end, jobId: {}, costMs: {}",
                jobDO.getId(),
                endTimeMillis - startTimeMillis);
    }

    @Override
    public WorkerStatus onRun() throws Exception {

        String jobId = getJobId();
        String workerId = getWorkerId();
        String workerArgs = getArgs();

        // // use hash policy to ensure the tasks belong to the same dag execute on the same worker,
        // and can make full use of the cache
        ServiceMeta.EntryPointMeta entryPoint =
                getLoadBalancer()
                        .selectService(
                                LoadBalancer.Policy.HASH,
                                Common.getServiceName(
                                        WeDPRCommonConfig.getAgency(), ServiceName.MPC.getValue()),
                                null,
                                jobId);
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
            MpcClient mpcClient = new MpcClient(url);
            // submit task, sync call
            String taskId = mpcClient.submitTask(workerArgs);
            mpcClient.pollTask(taskId);
        } finally {
            long endTimeMillis = System.currentTimeMillis();
            logger.info(
                    "## mpc engine run end, jobId: {}, workerId: {}, elapsedMs: {}",
                    jobId,
                    workerId,
                    (endTimeMillis - startTimeMillis));
        }

        return WorkerStatus.SUCCESS;
    }

    @SneakyThrows
    @Override
    public void onFinished() {

        super.onFinished();

        String workerArgs = getArgs();
        MpcRunJobRequest mpcRunJobRequest =
                ObjectMapperFactory.getObjectMapper().readValue(workerArgs, MpcRunJobRequest.class);
        boolean receiveResult = mpcRunJobRequest.isReceiveResult();
        if (!receiveResult) {
            logger.info(
                    "## mpc party not receive result, jobId: {}, workId: {}",
                    getJobId(),
                    getWorkerId());
            return;
        }

        long startTimeMillis = System.currentTimeMillis();

        logger.info(
                "## mpc worker on finished, jobId: {}, workerId: {}, args: {}",
                getJobId(),
                getWorkerId(),
                workerArgs);

        String outputFilePath = mpcRunJobRequest.getOutputFilePath();
        String resultFilePath = mpcRunJobRequest.getResultFilePath();

        String owner = mpcRunJobRequest.getOwner();
        String userGroup = null;
        String mpcOutputFilePath =
                Common.joinPath(
                        ExecutorConfig.getJobCacheDir(getJobId()),
                        ExecutorConfig.getMpcOutputFileName());
        String mpcResultFilePath =
                Common.joinPath(
                        ExecutorConfig.getJobCacheDir(getJobId()),
                        ExecutorConfig.getMpcResultFileName());

        try {
            // 1. download mpc_result.txt
            logger.info(
                    "begin to download mpc output file from {}=>{}, jobId: {}",
                    outputFilePath,
                    mpcOutputFilePath,
                    getJobId());

            FileStorageInterface fileStorage = getFileStorageInterface();
            HDFSStoragePath hdfsStoragePath = new HDFSStoragePath(outputFilePath);
            fileStorage.download(hdfsStoragePath, mpcOutputFilePath);

            logger.info("download the mpc output file successfully, jobId: {}", getJobId());

            // 2. trans mpc_result.txt to mpc_result.csv
            logger.info(
                    "begin to trans mpc output file to mpc result file from {}=>{}, jobId: {}",
                    mpcOutputFilePath,
                    mpcResultFilePath,
                    getJobId());

            MpcResultFileResolver mpcResultFileResolver = new MpcResultFileResolver();
            mpcResultFileResolver.transMpcOutputFile2ResultFile(
                    getJobId(), mpcOutputFilePath, mpcResultFilePath);

            logger.info(
                    "trans mpc output file to mpc result file successfully, jobId: {}", getJobId());

            // 3. upload mpc_result.csv to storage
            logger.info(
                    "begin to upload mpc result file from {}=>{}, jobId: {}",
                    mpcResultFilePath,
                    resultFilePath,
                    getJobId());

            FileStorageInterface.FilePermissionInfo permissionInfo =
                    new FileStorageInterface.FilePermissionInfo(owner, userGroup);
            fileStorage.upload(
                    permissionInfo, Boolean.TRUE, mpcResultFilePath, resultFilePath, true);

            logger.info("upload mpc result file successfully, jobId: {}", getJobId());
        } finally {
            // 4. remove temp file
            Common.deleteFile(new File(mpcOutputFilePath));
            Common.deleteFile(new File(mpcResultFilePath));

            long endTimeMillis = System.currentTimeMillis();

            logger.info(
                    "## mpc worker on finished end, jobId: {}, costMs: {}",
                    getJobId(),
                    (endTimeMillis - startTimeMillis));
        }
    }
}
