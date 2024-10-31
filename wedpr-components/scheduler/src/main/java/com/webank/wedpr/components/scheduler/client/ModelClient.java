package com.webank.wedpr.components.scheduler.client;

import static com.webank.wedpr.components.scheduler.client.common.ClientCommon.*;

import com.webank.wedpr.common.utils.BaseResponse;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.http.client.HttpClientImpl;
import com.webank.wedpr.components.scheduler.dag.entity.JobWorker;
import com.webank.wedpr.components.scheduler.dag.utils.WorkerUtils;
import com.webank.wedpr.components.scheduler.executor.impl.ml.MLExecutorConfig;
import com.webank.wedpr.components.scheduler.executor.impl.ml.request.ModelJobRequest;
import com.webank.wedpr.components.scheduler.executor.impl.ml.response.MLResponse;
import com.webank.wedpr.components.scheduler.executor.impl.ml.response.MLResponseFactory;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelClient {

    private static final Logger logger = LoggerFactory.getLogger(ModelClient.class);

    private final int pollIntervalMilli = DEFAULT_HTTP_POLL_TASK_INTERVAL_MILLI;
    private final int httpRetryTimes = DEFAULT_HTTP_REQUEST_MAX_RETRY_TIMES;
    private final int httpRetryDelayMilli = DEFAULT_HTTP_REQUEST_RETRY_DELAY_MILLI;

    private final HttpClientImpl httpClient;
    private final String url;

    public ModelClient(String url) {
        this.url = url;
        this.httpClient =
                new HttpClientImpl(
                        url,
                        MLExecutorConfig.getMaxTotalConnection(),
                        MLExecutorConfig.buildConfig(),
                        new MLResponseFactory());
    }

    public String submitTask(String params, JobWorker jobWorker) throws Exception {

        logger.info(
                "begin submit job to ML node, jobRequest: {}, workerId: {}",
                params,
                jobWorker.getWorkerId());

        ModelJobRequest modelJobRequest =
                ObjectMapperFactory.getObjectMapper().readValue(params, ModelJobRequest.class);

        String taskId = jobWorker.getWorkerId();
        String requestUrl = MLExecutorConfig.getRunTaskApiUrl(url, taskId);

        logger.info("taskId: {}, requestUrl: {}", taskId, requestUrl);

        String strResponse = sendRequestWithRetry(requestUrl, taskId, params);
        MLResponse response = MLResponse.deserialize(strResponse);

        if (response.statusOk()) {
            logger.info("submit ML job successfully, taskId: {}, jobRequest: {}", taskId, params);
            return taskId;
        }

        logger.error(
                "submit ML job failed, jobId: {}, jobRequest: {}, response: {}",
                taskId,
                params,
                response);

        throw new WeDPRException("submit ML job " + taskId + " failed for " + strResponse);
    }

    @SneakyThrows
    public void pollTask(String taskId) throws WeDPRException {

        String requestUrl = MLExecutorConfig.getRunTaskApiUrl(url, taskId);

        logger.info("taskId: {}, requestUrl: {}", taskId, requestUrl);

        while (true) {
            MLResponse response = (MLResponse) sendRequestWithRetry(requestUrl, taskId);

            if (!response.statusOk()) {
                logger.error(
                        "query ML job status error, taskId: {}, response: {}", taskId, response);
                throw new WeDPRException(
                        "query ML task status error, taskId: "
                                + taskId
                                + " ,response: "
                                + response);
            }

            if (response.failed()) {
                logger.error(
                        "query ML job status job execute failed, taskId: {}, response: {}",
                        taskId,
                        response);

                throw new WeDPRException(
                        "query ML task status error, taskId: "
                                + taskId
                                + " ,response: "
                                + response);
            }

            if (response.success()) {
                return;
            }

            // task is running

            WorkerUtils.sleep(pollIntervalMilli);
        }
    }

    /**
     * send request with retries
     *
     * @param params
     * @return
     * @throws WeDPRException
     */
    String sendRequestWithRetry(String url, String taskId, String params) throws WeDPRException {

        int retryTimes = 1;
        if (this.httpRetryTimes > 0) {
            retryTimes = this.httpRetryTimes;
        }

        int attempTimes = 0;
        while (attempTimes++ < retryTimes) {
            try {
                return httpClient.executePostAndGetString(url, params, Constant.HTTP_SUCCESS);
            } catch (Exception e) {
                if (attempTimes < retryTimes) {
                    WorkerUtils.sleep(httpRetryDelayMilli);
                } else {
                    logger.error("post request to ML node failed, taskId: {}, e: ", taskId, e);
                    throw new WeDPRException(
                            "post request to ML node failed, error: " + e.getMessage());
                }
            }
        }

        logger.error("post request to ML node failed, taskId: {}", taskId);
        throw new WeDPRException("post request to ML node failed, taskId: " + taskId);
    }

    /**
     * send request with retries
     *
     * @return
     * @throws WeDPRException
     */
    BaseResponse sendRequestWithRetry(String url, String taskId) throws WeDPRException {

        int retryTimes = 1;
        if (this.httpRetryTimes > 0) {
            retryTimes = this.httpRetryTimes;
        }

        int attempTimes = 0;
        while (attempTimes++ < retryTimes) {
            try {
                return httpClient.execute(url, false);
            } catch (Exception e) {
                if (attempTimes < retryTimes) {
                    WorkerUtils.sleep(httpRetryDelayMilli);
                } else {
                    logger.error("post request to ML node failed, taskId: {}, e: ", taskId, e);
                    throw new WeDPRException(
                            "post request to ML node failed, error: " + e.getMessage());
                }
            }
        }

        logger.error("post request to ML node failed, taskId: {}", taskId);
        throw new WeDPRException("post request to ML node failed, taskId: " + taskId);
    }
}
