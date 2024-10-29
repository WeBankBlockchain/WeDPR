package com.webank.wedpr.components.scheduler.client;

import static com.webank.wedpr.components.scheduler.client.common.ClientCommon.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.http.client.JsonRpcClient;
import com.webank.wedpr.components.http.client.model.JsonRpcResponse;
import com.webank.wedpr.components.scheduler.dag.utils.WorkerUtils;
import com.webank.wedpr.components.scheduler.executor.impl.psi.PSIExecutorConfig;
import com.webank.wedpr.components.scheduler.executor.impl.psi.model.PSIRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PsiClient {

    private static final Logger logger = LoggerFactory.getLogger(PsiClient.class);

    public static class QueryTaskParam {
        private String taskID;

        public QueryTaskParam() {}

        public QueryTaskParam(String taskID) {
            this.taskID = taskID;
        }

        public String getTaskID() {
            return taskID;
        }

        public void setTaskID(String taskID) {
            this.taskID = taskID;
        }

        public String serialize() throws JsonProcessingException {
            return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
        }
    }

    private static final String RUN_FINISHED_STATUS = "COMPLETED";

    private final int pollIntervalMilli = DEFAULT_HTTP_POLL_TASK_INTERVAL_MILLI;
    private final int httpRetryTimes = DEFAULT_HTTP_REQUEST_MAX_RETRY_TIMES;
    private final int httpRetryDelayMilli = DEFAULT_HTTP_REQUEST_RETRY_DELAY_MILLI;

    private final JsonRpcClient jsonRpcClient;

    public PsiClient(String url) {
        this.jsonRpcClient =
                new JsonRpcClient(
                        url,
                        PSIExecutorConfig.getMaxTotalConnection(),
                        PSIExecutorConfig.buildConfig());
    }

    public JsonRpcClient getJsonRpcClient() {
        return this.jsonRpcClient;
    }

    public String submitTask(String params) throws Exception {

        logger.info("begin submit job to PSI, jobRequest: {}", params);

        PSIRequest psiRequest =
                ObjectMapperFactory.getObjectMapper().readValue(params, PSIRequest.class);

        String taskId = psiRequest.getTaskID();

        String psiRunTaskMethod = PSIExecutorConfig.getPsiRunTaskMethod();
        String psiToken = PSIExecutorConfig.getPsiToken();
        JsonRpcResponse response =
                sendRequestWithRetry(taskId, psiRunTaskMethod, psiToken, psiRequest);
        if (response.statusOk()) {
            logger.info("submit PSI job successfully, taskId: {}, jobRequest: {}", taskId, params);
            return taskId;
        }

        if (response.getResult().getCode().equals(Constant.DuplicatedTaskCode)) {
            logger.info(
                    "PSI job has already been submitted, taskId: {}, jobRequest: {}",
                    taskId,
                    params);
            return taskId;
        }

        logger.error(
                "submit PSI job failed, jobId: {}, jobRequest: {}, response: {}",
                taskId,
                params,
                response);

        throw new WeDPRException(
                "submit PSI job " + taskId + " failed for " + response.getResult().getMessage());
    }

    public void pollTask(String taskId) throws WeDPRException {
        while (true) {
            JsonRpcResponse response =
                    sendRequestWithRetry(
                            taskId,
                            PSIExecutorConfig.getPsiGetTaskStatusMethod(),
                            PSIExecutorConfig.getPsiToken(),
                            new QueryTaskParam(taskId));

            // response error
            if (!response.statusOk()) {
                logger.warn("query PSI status error, taskId: {}, response: {}", taskId, response);
                throw new WeDPRException(
                        "query PSI task status error, taskId: "
                                + taskId
                                + " ,response: "
                                + response);
            }

            if (response.getResult().getStatus().compareToIgnoreCase(RUN_FINISHED_STATUS) == 0) {
                logger.info(
                        "PSI task execute successfully, taskId: {}, response: {}",
                        taskId,
                        response);
                return;
            }

            WorkerUtils.sleep(pollIntervalMilli);
        }
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
                    logger.error("post request to PSI failed, taskId: {}, e: ", taskId, e);
                    throw new WeDPRException(
                            "post request to PSI failed, error: " + e.getMessage());
                }
            }
        }

        logger.error("post request to PSI failed, taskId: {}", taskId);
        throw new WeDPRException("post request to PSI node failed, taskId: " + taskId);
    }
}
