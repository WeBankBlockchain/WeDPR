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

import com.webank.wedpr.components.http.client.model.JsonRpcRequest;
import com.webank.wedpr.components.http.client.model.JsonRpcResponse;
import com.webank.wedpr.components.http.client.model.JsonRpcResponseFactory;
import org.apache.http.client.config.RequestConfig;

public class JsonRpcClient {
    private final HttpClientImpl httpClient;

    public JsonRpcClient(String url, Integer maxConnTotal, RequestConfig requestConfig) {
        this.httpClient =
                new HttpClientImpl(url, maxConnTotal, requestConfig, new JsonRpcResponseFactory());
    }

    public JsonRpcResponse post(String token, String method, Object params) throws Exception {
        JsonRpcRequest request = new JsonRpcRequest();
        request.setToken(token);
        request.setMethod(method);
        request.setParams(params);
        return (JsonRpcResponse) this.httpClient.executePost(request);
    }
}
