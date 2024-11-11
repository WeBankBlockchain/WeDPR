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

package com.webank.wedpr.components.scheduler.executor.impl.mpc;

import com.webank.wedpr.common.config.WeDPRConfig;
import org.apache.http.client.config.RequestConfig;

public class MPCExecutorConfig {
    //    private static final String MPC_URL = WeDPRConfig.apply("wedpr.executor.mpc.url", null,
    // true);
    private static final String MPC_RUN_TASK_METHOD =
            WeDPRConfig.apply("wedpr.executor.mpc.method.runTask", "run");
    private static final String MPC_KILL_TASK_METHOD =
            WeDPRConfig.apply("wedpr.executor.mpc.method.killTask", "kill");
    private static final String MPC_TOKEN = WeDPRConfig.apply("wedpr.executor.mpc.token", "", true);
    private static final String MPC_DIRECT_NODE_IP =
            WeDPRConfig.apply("wedpr.executor.mpc.direct.ip", "", true);
    private static final String MPC_DIRECT_NODE_PORT =
            WeDPRConfig.apply("wedpr.executor.mpc.direct.port", "", true);

    private static final String MPC_IS_MALICIOUS =
            WeDPRConfig.apply("wedpr.executor.mpc.is.malicious", "false");

    private static final Integer CONNECTION_REQUEST_TIME_OUT =
            WeDPRConfig.apply("wedpr.executor.mpc.connect.request.timeout.ms", 10000);
    private static final Integer CONNECTION_TIME_OUT =
            WeDPRConfig.apply("wedpr.executor.mpc.connect.timeout.ms", 5000);
    private static final Integer REQUEST_TIMEOUT =
            WeDPRConfig.apply("wedpr.executor.mpc.request.timeout.ms", 3600000);

    private static final Integer MAX_TOTAL_CONNECTION =
            WeDPRConfig.apply("wedpr.executor.mpc.max.total.connection", 5);

    public static RequestConfig buildConfig() {
        return RequestConfig.custom()
                .setConnectionRequestTimeout(CONNECTION_REQUEST_TIME_OUT)
                .setConnectTimeout(CONNECTION_TIME_OUT)
                .setSocketTimeout(REQUEST_TIMEOUT)
                .build();
    }

    //    public static String getMpcUrl() {
    //        return MPC_URL;
    //    }

    public static String getMpcRunTaskMethod() {
        return MPC_RUN_TASK_METHOD;
    }

    public static String getMpcKillTaskMethod() {
        return MPC_KILL_TASK_METHOD;
    }

    public static String getMpcToken() {
        return MPC_TOKEN;
    }

    public static Integer getMaxTotalConnection() {
        return MAX_TOTAL_CONNECTION;
    }

    public static String getMpcDirectNodeIp() {
        return MPC_DIRECT_NODE_IP;
    }

    public static int getMpcDirectNodePort() {
        return Integer.parseInt(MPC_DIRECT_NODE_PORT);
    }

    public static Boolean getMpcIsMalicious() {
        return Boolean.valueOf(MPC_IS_MALICIOUS);
    }
}
