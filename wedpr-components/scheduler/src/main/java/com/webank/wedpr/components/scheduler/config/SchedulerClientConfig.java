package com.webank.wedpr.components.scheduler.config;

import com.webank.wedpr.core.config.WeDPRConfig;
import org.apache.http.client.config.RequestConfig;

public class SchedulerClientConfig {

    private SchedulerClientConfig() {}

    private static final String DEFAULT_SCHEDULER_API_PATH = "/api/wedpr/v3/scheduler/";

    private static final String SCHEDULER_URL =
            WeDPRConfig.apply("wedpr.remote.scheduler.url", "", true);

    private static final String SCHEDULER_API_PATH =
            WeDPRConfig.apply("wedpr.remote.scheduler.api.path", DEFAULT_SCHEDULER_API_PATH);

    private static final Integer MAX_TOTAL_CONNECTION =
            WeDPRConfig.apply("wedpr.remote.scheduler.max.total.connection", 25);

    private static final Integer CONNECTION_REQUEST_TIME_OUT =
            WeDPRConfig.apply("wedpr.remote.scheduler.connect.request.timeout.ms", 10000);
    private static final Integer CONNECTION_TIME_OUT =
            WeDPRConfig.apply("wedpr.remote.scheduler.connect.timeout.ms", 5000);
    private static final Integer REQUEST_TIMEOUT =
            WeDPRConfig.apply("wedpr.remote.scheduler.request.timeout.ms", 60000);

    public static String getUrl() {
        return SCHEDULER_API_PATH;
    }

    public static String getSchedulerJobUrl(String jobID) {
        if (SCHEDULER_API_PATH.endsWith("/")) {
            return SCHEDULER_URL + SCHEDULER_API_PATH + "job/" + jobID;
        }
        return SCHEDULER_URL + SCHEDULER_API_PATH + "/job/" + jobID;
    }

    public static String getSchedulerSqlUrl() {
        if (SCHEDULER_API_PATH.endsWith("/")) {
            return SCHEDULER_URL + SCHEDULER_API_PATH + "sql/transfer";
        }
        return SCHEDULER_URL + SCHEDULER_API_PATH + "/sql/transfer";
    }

    public static int getMaxTotalConnection() {
        return MAX_TOTAL_CONNECTION;
    }

    public static RequestConfig buildConfig() {
        return RequestConfig.custom()
                .setConnectionRequestTimeout(CONNECTION_REQUEST_TIME_OUT)
                .setConnectTimeout(CONNECTION_TIME_OUT)
                .setSocketTimeout(REQUEST_TIMEOUT)
                .build();
    }
}
