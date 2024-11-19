package com.webank.wedpr.components.scheduler.client;

import static com.webank.wedpr.components.scheduler.client.common.ClientCommon.DEFAULT_HTTP_POLL_TASK_INTERVAL_MILLI;
import static com.webank.wedpr.components.scheduler.client.common.ClientCommon.DEFAULT_HTTP_REQUEST_MAX_RETRY_TIMES;
import static com.webank.wedpr.components.scheduler.client.common.ClientCommon.DEFAULT_HTTP_REQUEST_RETRY_DELAY_MILLI;

import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.http.client.JsonRpcClient;
import com.webank.wedpr.components.http.client.model.JsonRpcResponse;
import com.webank.wedpr.components.scheduler.dag.utils.WorkerUtils;
import com.webank.wedpr.components.scheduler.executor.impl.mpc.MPCExecutorConfig;
import com.webank.wedpr.components.scheduler.executor.impl.mpc.request.MpcKillJobRequest;
import com.webank.wedpr.components.scheduler.executor.impl.mpc.request.MpcQueryJobRequest;
import com.webank.wedpr.components.scheduler.executor.impl.mpc.request.MpcRunJobRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MpcClient {

    private static final Logger logger = LoggerFactory.getLogger(MpcClient.class);

    private final int pollIntervalMilli = DEFAULT_HTTP_POLL_TASK_INTERVAL_MILLI;
    private final int httpRetryTimes = DEFAULT_HTTP_REQUEST_MAX_RETRY_TIMES;
    private final int httpRetryDelayMilli = DEFAULT_HTTP_REQUEST_RETRY_DELAY_MILLI;

    private static final String RUN_FINISHED_STATUS = "COMPLETED";

    private final Integer MpcSuccessStatus = 0;
    private final Integer MpcDuplicatedTaskStatus = 1;
    private final Integer MpcFailedStatus = -1;

    private final JsonRpcClient jsonRpcClient;

    public MpcClient(String url) {
        this.jsonRpcClient =
                new JsonRpcClient(
                        url,
                        MPCExecutorConfig.getMaxTotalConnection(),
                        MPCExecutorConfig.buildConfig());
    }

    public String submitTask(String params) throws Exception {

        logger.info("begin submit job to MPC, jobRequest: {}", params);

        MpcRunJobRequest mpcRunJobRequest =
                ObjectMapperFactory.getObjectMapper().readValue(params, MpcRunJobRequest.class);

        String jobId = mpcRunJobRequest.getJobId();

        String mpcRunTaskMethod = MPCExecutorConfig.getMpcRunTaskMethod();
        String mpcToken = MPCExecutorConfig.getMpcToken();
        JsonRpcResponse response =
                sendRequestWithRetry(jobId, mpcRunTaskMethod, mpcToken, mpcRunJobRequest);
        if (response.statusOk()) {
            logger.info("submit MPC job successfully, taskId: {}, jobRequest: {}", jobId, params);
            return jobId;
        }

        if (response.getResult().getCode().equals(MpcDuplicatedTaskStatus)) {
            logger.info(
                    "MPC job has already been submitted, taskId: {}, jobRequest: {}",
                    jobId,
                    params);
            return jobId;
        }

        logger.error(
                "submit MPC job failed, jobId: {}, jobRequest: {}, response: {}",
                jobId,
                params,
                response);

        throw new WeDPRException(
                "submit MPC job " + jobId + " failed for " + response.getResult().getMessage());
    }

    public void pollTask(String taskId) throws WeDPRException {
        String mpcRunTaskMethod = MPCExecutorConfig.getMpcRunTaskMethod();
        String mpcToken = MPCExecutorConfig.getMpcToken();
        MpcQueryJobRequest mpcQueryJobRequest = new MpcQueryJobRequest();
        mpcQueryJobRequest.setJobId(taskId);

        while (true) {
            JsonRpcResponse response =
                    sendRequestWithRetry(taskId, mpcRunTaskMethod, mpcToken, mpcQueryJobRequest);

            // response error
            if (!response.statusOk()) {
                logger.warn("query MPC status error, taskId: {}, response: {}", taskId, response);
                throw new WeDPRException(
                        "query MPC task status error, taskId: "
                                + taskId
                                + " ,response: "
                                + response);
            }

            if (response.getResult().getStatus().compareToIgnoreCase(RUN_FINISHED_STATUS) == 0) {
                logger.info(
                        "MPC task execute successfully, taskId: {}, response: {}",
                        taskId,
                        response);
                return;
            }

            WorkerUtils.sleep(pollIntervalMilli);
        }
    }

    public void killTask(String jobId) throws WeDPRException {

        String mpcKillTaskMethod = MPCExecutorConfig.getMpcKillTaskMethod();
        String mpcToken = MPCExecutorConfig.getMpcToken();

        MpcKillJobRequest mpcKillJobRequest = new MpcKillJobRequest();
        mpcKillJobRequest.setJobId(jobId);

        JsonRpcResponse response =
                sendRequestWithRetry(jobId, mpcKillTaskMethod, mpcToken, mpcKillJobRequest);
        //        if (response.statusOk()) {
        //            logger.info("submit MPC job successfully, taskId: {}, jobRequest: {}", jobId,
        // params);
        //            return jobId;
        //        }

        logger.info("submit kill MPC job, jobId: {}, response: {}", jobId, response);
    }

    /**
     * send request with retries
     *
     * @param method
     * @param token
     * @param params
     * @return
     * @throws WeDPRException
     */
    JsonRpcResponse sendRequestWithRetry(String taskId, String method, String token, Object params)
            throws WeDPRException {

        int retryTimes = 1;
        if (this.httpRetryTimes > 0) {
            retryTimes = this.httpRetryTimes;
        }

        int attempTimes = 0;
        while (attempTimes++ < retryTimes) {
            try {
                return this.jsonRpcClient.post(token, method, params);
            } catch (Exception e) {
                if (attempTimes < retryTimes) {
                    WorkerUtils.sleep(httpRetryDelayMilli);
                } else {
                    logger.error("post request to MPC failed, taskId: {}, e: ", taskId, e);
                    throw new WeDPRException(
                            "post request to MPC failed, error: " + e.getMessage());
                }
            }
        }

        logger.error("post request to MPC failed, taskId: {}", taskId);
        throw new WeDPRException("post request to MPC node failed, taskId: " + taskId);
    }
}
