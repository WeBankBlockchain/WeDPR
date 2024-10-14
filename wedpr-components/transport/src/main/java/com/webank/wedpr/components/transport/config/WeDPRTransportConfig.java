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

package com.webank.wedpr.components.transport.config;

import com.webank.wedpr.common.config.WeDPRConfig;

public class WeDPRTransportConfig {
    private static Integer THREAD_POOL_SIZE =
            WeDPRConfig.apply("wedpr.transport.threadpool_size", 4);
    // the nodeID
    private static String NODEID = WeDPRConfig.apply("wedpr.transport.nodeID", "");

    private static String GATEWAY_TARGETS =
            WeDPRConfig.apply("wedpr.transport.gateway_targets", null);
    private static String HOST_IP = WeDPRConfig.apply("wedpr.transport.host_ip", null);
    private static String LISTEN_IP = WeDPRConfig.apply("wedpr.transport.listen_ip", "0.0.0.0");
    private static Integer LISTEN_PORT = WeDPRConfig.apply("wedpr.transport.listen_port", 6001);

    public static String getGatewayTargets() {
        return GATEWAY_TARGETS;
    }

    public static String getHostIp() {
        return HOST_IP;
    }

    public static String getListenIp() {
        return LISTEN_IP;
    }

    public static Integer getListenPort() {
        return LISTEN_PORT;
    }

    public static String getNODEID() {
        return NODEID;
    }

    public static Integer getThreadPoolSize() {
        return THREAD_POOL_SIZE;
    }
}
