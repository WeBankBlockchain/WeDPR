/*
 * Copyright 2017-2025  [webank-wedpr]
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.webank.wedpr.components.scheduler.executor.impl.ml;

import com.webank.wedpr.common.config.WeDPRConfig;
import org.apache.http.client.config.RequestConfig;

public class MLExecutorConfig {
    private static final String DEFAULT_RUNTASK_API_PATH = "/api/ppc-model/pml/run-model-task/";
    private static final String DEFAULT_GET_JOB_RESULT_API_PATH =
            "api/ppc-model/pml/get-job-result/";
    private static final String URL = WeDPRConfig.apply("wedpr.executor.ml.url", null, true);
    private static final String RUN_TASK_API_PATH =
            WeDPRConfig.apply("wedpr.executor.ml.method.runTask", DEFAULT_RUNTASK_API_PATH);

    private static final String OBTAIN_JOB_RESULT_API_PATH =
            WeDPRConfig.apply(
                    "wedpr.executor.ml.method.getJobResult", DEFAULT_GET_JOB_RESULT_API_PATH);
    private static final String SUCCESS_STATUS =
            WeDPRConfig.apply("wedpr.executor.ml.status.success", "COMPLETED");
    private static final String FAILED_STATUS =
            WeDPRConfig.apply("wedpr.executor.ml.status.failed", "FAILED");
    private static final Integer CONNECTION_REQUEST_TIME_OUT =
            WeDPRConfig.apply("wedpr.executor.ml.connect.request.timeout.ms", 10000);
    private static final Integer CONNECTION_TIME_OUT =
            WeDPRConfig.apply("wedpr.executor.ml.connect.timeout.ms", 5000);
    private static final Integer REQUEST_TIMEOUT =
            WeDPRConfig.apply("wedpr.executor.ml.request.timeout.ms", 60000);
    private static final Integer MAX_TOTAL_CONNECTION =
            WeDPRConfig.apply("wedpr.executor.ml.max.total.connection", 5);

    public static RequestConfig buildConfig() {
        return RequestConfig.custom()
                .setConnectionRequestTimeout(CONNECTION_REQUEST_TIME_OUT)
                .setConnectTimeout(CONNECTION_TIME_OUT)
                .setSocketTimeout(REQUEST_TIMEOUT)
                .build();
    }

    public static String getUrl() {
        return URL;
    }

    public static String getRunTaskApiUrl(String jobID) {
        return URL + "/" + RUN_TASK_API_PATH + jobID;
    }

    public static String getObtainJobResultApiUrl(String jobID) {
        return URL + "/" + OBTAIN_JOB_RESULT_API_PATH + "/" + jobID;
    }

    public static String getSuccessStatus() {
        return SUCCESS_STATUS;
    }

    public static String getFailedStatus() {
        return FAILED_STATUS;
    }

    public static Integer getMaxTotalConnection() {
        return MAX_TOTAL_CONNECTION;
    }
}
