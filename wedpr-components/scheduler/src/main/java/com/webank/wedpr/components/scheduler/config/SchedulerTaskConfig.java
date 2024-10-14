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

package com.webank.wedpr.components.scheduler.config;

import com.webank.wedpr.common.config.WeDPRConfig;

public class SchedulerTaskConfig {
    private static final Integer WORKER_QUEUE_SIZE =
            WeDPRConfig.apply("wedpr.thread.max.blocking.queue.size", 1000);
    private static final Integer JOB_CONCURRENCY =
            WeDPRConfig.apply("wedpr.scheduler.job.concurrency", 5);
    private static final Integer QUERY_JOB_STATUS_INTERVAL_MS =
            WeDPRConfig.apply("wedpr.scheduler.query.job.status.interval.ms", 30000);
    private static final Integer SCHEDULER_INTERVAL_MS =
            WeDPRConfig.apply("wedpr.scheduler.interval.ms", 30000);

    public static Integer getWorkerQueueSize() {
        return WORKER_QUEUE_SIZE;
    }

    public static Integer getJobConcurrency() {
        return JOB_CONCURRENCY;
    }

    public static Integer getQueryJobStatusIntervalMs() {
        return QUERY_JOB_STATUS_INTERVAL_MS;
    }

    public static Integer getSchedulerIntervalMs() {
        return SCHEDULER_INTERVAL_MS;
    }
}
