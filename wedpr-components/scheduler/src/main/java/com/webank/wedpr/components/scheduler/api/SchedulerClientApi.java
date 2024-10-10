package com.webank.wedpr.components.scheduler.api;

import com.webank.wedpr.components.scheduler.executor.ExecuteResult;

public interface SchedulerClientApi {
    void postJob(String jobId, String request) throws Exception;

    void killJob(String jobId) throws Exception;

    ExecuteResult queryJob(String jobId) throws Exception;

    String transferSQL2MPCContent(String jobId, String sql) throws Exception;
}
