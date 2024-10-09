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

package com.webank.wedpr.components.publish.config;

import com.webank.wedpr.core.config.WeDPRConfig;
import com.webank.wedpr.core.utils.Constant;
import org.apache.http.client.config.RequestConfig;

public class ServicePublisherConfig {
    // default 30
    private static final Integer SERVICE_SYNCER_BATCH_SIZE =
            WeDPRConfig.apply("wedpr.service.syncer.batch.size", 30);

    private static final String PIR_PUBLISH_SERVICE_URI_PATH =
            WeDPRConfig.apply(
                    "wedpr.service.uri.path.pir_publish",
                    Constant.WEDPR_API_PREFIX + "/pir/publish");

    /// the service worker client config ///
    private static final Integer CONNECTION_REQUEST_TIME_OUT =
            WeDPRConfig.apply("wedpr.service.connect.request.timeout.ms", 10000);
    private static final Integer CONNECTION_TIME_OUT =
            WeDPRConfig.apply("wedpr.service.connect.timeout.ms", 5000);
    private static final Integer REQUEST_TIMEOUT =
            WeDPRConfig.apply("wedpr.service.request.timeout.ms", 60000);
    private static final Integer MAX_TOTAL_CONNECTION =
            WeDPRConfig.apply("wedpr.service.max.total.connection", 5);
    /// the service worker client config ///
    public static Integer getServiceSyncerBatchSize() {
        return SERVICE_SYNCER_BATCH_SIZE;
    }

    public static RequestConfig buildConfig() {
        return RequestConfig.custom()
                .setConnectionRequestTimeout(CONNECTION_REQUEST_TIME_OUT)
                .setConnectTimeout(CONNECTION_TIME_OUT)
                .setSocketTimeout(REQUEST_TIMEOUT)
                .build();
    }

    public static String getPirPublishServiceUriPath() {
        return PIR_PUBLISH_SERVICE_URI_PATH;
    }

    public static Integer getMaxTotalConnection() {
        return MAX_TOTAL_CONNECTION;
    }
}
