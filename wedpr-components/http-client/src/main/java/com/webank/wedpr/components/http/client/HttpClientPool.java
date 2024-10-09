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

package com.webank.wedpr.components.http.client;

import java.io.IOException;
import java.io.InputStream;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class HttpClientPool {
    private static final PoolingHttpClientConnectionManager poolingHttpClientConnectionManager =
            new PoolingHttpClientConnectionManager();

    public static CloseableHttpClient getHttpClient(int maxConnTotal, RequestConfig requestConfig) {
        return HttpClients.custom()
                .setConnectionManager(poolingHttpClientConnectionManager)
                .setMaxConnTotal(maxConnTotal)
                .setMaxConnPerRoute((maxConnTotal / 2 > 0 ? maxConnTotal / 2 : 1))
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    public static void consume(HttpEntity entity) throws IOException {
        if (entity == null) {
            return;
        }
        if (!entity.isStreaming()) {
            return;
        }
        InputStream inStream = entity.getContent();
        if (inStream != null) {
            inStream.close();
        }
    }
}
