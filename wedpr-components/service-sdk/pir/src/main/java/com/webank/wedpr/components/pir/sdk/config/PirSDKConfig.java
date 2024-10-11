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

package com.webank.wedpr.components.pir.sdk.config;

import com.webank.wedpr.core.config.WeDPRCommonConfig;
import com.webank.wedpr.core.config.WeDPRConfig;
import com.webank.wedpr.core.utils.ThreadPoolService;

public class PirSDKConfig {
    private static String PIR_TOPIC_PREFIX = "PIR_TOPIC_";
    private static String PIR_COMPONENT_PREFIX = "PIR_COMPONENT_";
    private static Integer PIR_QUERY_TIMEOUT_MS =
            WeDPRConfig.apply("wedpr.service.pir.timeout_ms", 30000);

    private static String PIR_CACHE_DIR = WeDPRConfig.apply("wedpr.pir.cache.dir", ".cache");
    private static Integer PIR_THREAD_POOL_QUEUE_SIZE_LIMIT =
            WeDPRConfig.apply("wedpr.pir.threadpool.queue.size.limit", 10000);

    private static final ThreadPoolService threadPoolService =
            new ThreadPoolService("pir-workers", PirSDKConfig.getPirThreadPoolQueueSizeLimit());

    public static Integer getPirThreadPoolQueueSizeLimit() {
        return PIR_THREAD_POOL_QUEUE_SIZE_LIMIT;
    }

    public static ThreadPoolService getThreadPoolService() {
        return threadPoolService;
    }

    public static String getPirComponent(String serviceID) {
        return PIR_COMPONENT_PREFIX + WeDPRCommonConfig.getAgency() + "_" + serviceID;
    }

    public static String getPirTopic(String serviceID) {
        return PIR_TOPIC_PREFIX + serviceID;
    }

    public static Integer getPirQueryTimeoutMs() {
        return PIR_QUERY_TIMEOUT_MS;
    }
}
