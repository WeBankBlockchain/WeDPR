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

package com.webank.wedpr.components.scheduler.executor.impl.psi;

import com.webank.wedpr.core.config.WeDPRConfig;
import org.apache.http.client.config.RequestConfig;

public class PSIExecutorConfig {
    private static final String PSI_URL = WeDPRConfig.apply("wedpr.executor.psi.url", null, true);
    private static final String PSI_RUN_TASK_METHOD =
            WeDPRConfig.apply("wedpr.executor.psi.method.runTask", "asyncRunTask");
    private static final String PSI_GET_TASK_STATUS_METHOD =
            WeDPRConfig.apply("wedpr.executor.psi.method.getStatus", "getTaskStatus");

    private static final Integer CONNECTION_REQUEST_TIME_OUT =
            WeDPRConfig.apply("wedpr.executor.psi.connect.request.timeout.ms", 10000);
    private static final Integer CONNECTION_TIME_OUT =
            WeDPRConfig.apply("wedpr.executor.psi.connect.timeout.ms", 5000);
    private static final Integer REQUEST_TIMEOUT =
            WeDPRConfig.apply("wedpr.executor.psi.request.timeout.ms", 60000);
    private static final String PSI_TOKEN =
            WeDPRConfig.apply("wedpr.executor.psi.token", null, true);
    private static final Integer MAX_TOTAL_CONNECTION =
            WeDPRConfig.apply("wedpr.executor.psi.max.total.connection", 5);

    public static RequestConfig buildConfig() {
        return RequestConfig.custom()
                .setConnectionRequestTimeout(CONNECTION_REQUEST_TIME_OUT)
                .setConnectTimeout(CONNECTION_TIME_OUT)
                .setSocketTimeout(REQUEST_TIMEOUT)
                .build();
    }

    public static String getPsiUrl() {
        return PSI_URL;
    }

    public static String getPsiRunTaskMethod() {
        return PSI_RUN_TASK_METHOD;
    }

    public static String getPsiGetTaskStatusMethod() {
        return PSI_GET_TASK_STATUS_METHOD;
    }

    public static String getPsiToken() {
        return PSI_TOKEN;
    }

    public static Integer getMaxTotalConnection() {
        return MAX_TOTAL_CONNECTION;
    }
}
