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

package com.webank.wedpr.components.task.plugin.pir.config;

import com.webank.wedpr.core.config.WeDPRConfig;
import com.webank.wedpr.core.utils.ThreadPoolService;

public class PirServiceConfig {
    private static String PIR_CACHE_DIR = WeDPRConfig.apply("wedpr.pir.cache.dir", ".cache");
    private static Integer PIR_THREAD_POOL_QUEUE_SIZE_LIMIT =
            WeDPRConfig.apply("wedpr.pir.threadpool.queue.size.limit", 10000);

    private static final ThreadPoolService threadPoolService =
            new ThreadPoolService("pir-workers", PirServiceConfig.getPirThreadPoolQueueSizeLimit());

    public static String getPirCacheDir() {
        return PIR_CACHE_DIR;
    }

    public static Integer getPirThreadPoolQueueSizeLimit() {
        return PIR_THREAD_POOL_QUEUE_SIZE_LIMIT;
    }

    public static ThreadPoolService getThreadPoolService() {
        return threadPoolService;
    }
}
