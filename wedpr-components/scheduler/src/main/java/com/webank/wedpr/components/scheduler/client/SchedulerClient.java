package com.webank.wedpr.components.scheduler.client;

import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.http.client.HttpClientImpl;
import com.webank.wedpr.components.scheduler.api.SchedulerClientApi;
import com.webank.wedpr.components.scheduler.client.request.TransferSQLRequest;
import com.webank.wedpr.components.scheduler.client.response.KillJobResponse;
import com.webank.wedpr.components.scheduler.client.response.PostJobResponse;
import com.webank.wedpr.components.scheduler.client.response.QueryJobResponse;
import com.webank.wedpr.components.scheduler.client.response.TransferSQLResponse;
import com.webank.wedpr.components.scheduler.config.SchedulerClientConfig;
import com.webank.wedpr.components.scheduler.executor.ExecuteResult;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.mortbay.jetty.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchedulerClient implements SchedulerClientApi {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerClient.class);

    private final HttpClientImpl httpClient;

    private final int SCHEDULER_SERVICE_RESPONSE_OK = 0;

    public SchedulerClient() {
        this.httpClient =
                new HttpClientImpl(
                        "",
                        SchedulerClientConfig.getMaxTotalConnection(),
                        SchedulerClientConfig.buildConfig(),
                        null);
    }

    @Override
    public void postJob(String jobId, String request) throws Exception {

        String url = SchedulerClientConfig.getSchedulerJobUrl(jobId);
        String response = null;
        try {
            response =
                    this.httpClient.executePostAndGetString(
                            url, request, HttpStatus.ORDINAL_200_OK);

            if (logger.isDebugEnabled()) {
                logger.debug(
                        "post job, jobId: {}, url: {}, request: {}, response: {}",
                        jobId,
                        url,
                        request,
                        response);
            }

        } catch (Exception e) {
            logger.error("post job exception, jobId: {}, url: {}, e: ", jobId, url, e);
            throw e;
        }

        PostJobResponse postJobResponse =
                ObjectMapperFactory.getObjectMapper().readValue(response, PostJobResponse.class);

        if (postJobResponse.getErrorCode() != SCHEDULER_SERVICE_RESPONSE_OK) {
            logger.error("post job failed, jobId: {}, response: {}", jobId, postJobResponse);
            throw new WeDPRException("post job failed, errorMsg: " + postJobResponse.getMessage());
        } else {
            logger.debug("post job successfully, jobId: {}, response: {}", jobId, postJobResponse);
        }
    }

    @Override
    public void killJob(String jobId) throws Exception {

        String response = null;
        try {
            String url = SchedulerClientConfig.getSchedulerJobUrl(jobId);

            HttpDelete httpDelete = new HttpDelete(url);
            response = this.httpClient.executeAndGetString(httpDelete);

            if (logger.isDebugEnabled()) {
                logger.debug("kill job, jobId: {}, url: {}, response: {}", jobId, url, response);
            }

        } catch (Exception e) {
            logger.error("kill job exception, jobId: {}, e: ", jobId, e);
            throw e;
        }

        KillJobResponse killJobResponse =
                ObjectMapperFactory.getObjectMapper().readValue(response, KillJobResponse.class);

        if (killJobResponse.getErrorCode() != SCHEDULER_SERVICE_RESPONSE_OK) {
            logger.error("kill job failed, jobId: {}, response: {}", jobId, killJobResponse);
            throw new WeDPRException(
                    "kill job failed, jobId: "
                            + jobId
                            + ", errorMsg: "
                            + killJobResponse.getMessage());
        } else {
            logger.info("kill job successfully, jobId: {}, response: {}", jobId, killJobResponse);
        }
    }

    @Override
    public ExecuteResult queryJob(String jobId) throws Exception {

        String response = null;
        try {
            String url = SchedulerClientConfig.getSchedulerJobUrl(jobId);

            HttpGet httpGet = new HttpGet(url);
            response = this.httpClient.executeAndGetString(httpGet);

            if (logger.isInfoEnabled()) {
                logger.info("query job, jobId: {}, url: {}, response: {}", jobId, url, response);
            }

        } catch (Exception e) {
            logger.error("query job exception, jobId: {}, e: ", jobId, e);
            throw e;
        }

        QueryJobResponse queryJobResponse =
                ObjectMapperFactory.getObjectMapper().readValue(response, QueryJobResponse.class);

        if (queryJobResponse.getErrorCode() != SCHEDULER_SERVICE_RESPONSE_OK) {
            logger.error(
                    "query job status failed, jobId: {}, response: {}", jobId, queryJobResponse);
            throw new WeDPRException(
                    "query job status failed, jobId: "
                            + jobId
                            + ", errorMsg: "
                            + queryJobResponse.getMessage());
        } else {
            logger.info(
                    "query job status successfully, jobId: {}, response: {}",
                    jobId,
                    queryJobResponse);

            String status = queryJobResponse.getData().getStatus();

            ExecuteResult.ResultStatus resultStatus = ExecuteResult.ResultStatus.valueOf(status);

            ExecuteResult executeResult = new ExecuteResult();
            executeResult.setResultStatus(resultStatus);
            executeResult.setMsg(resultStatus.name());

            return executeResult;
        }
    }

    @Override
    public String transferSQL2MPCContent(String jobId, String sql) throws Exception {
        String strResponse = null;
        try {
            String url = SchedulerClientConfig.getSchedulerSqlUrl();

            TransferSQLRequest request = new TransferSQLRequest();
            request.setSql(sql);
            String strRequest = ObjectMapperFactory.getObjectMapper().writeValueAsString(request);

            strResponse =
                    this.httpClient.executePostAndGetString(
                            url, strRequest, HttpStatus.ORDINAL_200_OK);

            if (logger.isInfoEnabled()) {
                logger.info(
                        "transfer sql to mpc code, jobId: {}, url: {}, sql: {}, response: {}",
                        jobId,
                        url,
                        sql,
                        strResponse);
            }

        } catch (Exception e) {
            logger.error("transfer sql to mpc code exception, jobId: {}, e: ", jobId, e);
            throw e;
        }

        TransferSQLResponse response =
                ObjectMapperFactory.getObjectMapper()
                        .readValue(strResponse, TransferSQLResponse.class);

        if (response.getErrorCode() != SCHEDULER_SERVICE_RESPONSE_OK) {
            logger.error(
                    "transfer sql to mpc code failed, jobId: {}, sql: {}, response: {}",
                    jobId,
                    sql,
                    response);
            throw new WeDPRException(
                    "transfer sql to mpc code failed, jobId: "
                            + jobId
                            + ", errorMsg: "
                            + response.getMessage());
        } else {
            logger.info(
                    "transfer sql to mpc code successfully, jobId: {}, sql: {}, response: {}",
                    jobId,
                    sql,
                    response);

            return response.getData().getMpcContent();
        }
    }
}
