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

import com.webank.wedpr.core.utils.BaseRequest;
import com.webank.wedpr.core.utils.BaseResponse;
import com.webank.wedpr.core.utils.BaseResponseFactory;
import com.webank.wedpr.core.utils.WeDPRException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientImpl {
    private static final Logger logger = LoggerFactory.getLogger(HttpClientImpl.class);
    private static final String CONTENT_TYPE_KEY = "Content-Type";
    private static final String DEFAULT_CONTENT_TYPE = "application/json";
    private final String url;
    private final Integer maxConnTotal;
    private final RequestConfig requestConfig;
    private final BaseResponseFactory factory;

    public HttpClientImpl(
            String url,
            Integer maxConnTotal,
            RequestConfig requestConfig,
            BaseResponseFactory factory) {
        this.url = HttpClientPool.getUrl(url);
        this.maxConnTotal = maxConnTotal;
        this.requestConfig = requestConfig;
        this.factory = factory;
    }

    public String getUrl() {
        return this.url;
    }

    public BaseResponse executePost(BaseRequest request) throws Exception {
        return factory.build(executePostAndGetString(request, null));
    }

    public String executePostAndGetString(BaseRequest request, Integer successCode)
            throws Exception {
        StringEntity requestEntity = null;
        CloseableHttpResponse response = null;
        try {
            CloseableHttpClient httpClient =
                    HttpClientPool.getHttpClient(this.maxConnTotal, this.requestConfig);
            HttpPost httpPost = new HttpPost(this.url);
            requestEntity = new StringEntity(request.serialize());
            httpPost.setEntity(requestEntity);
            httpPost.setHeader(CONTENT_TYPE_KEY, DEFAULT_CONTENT_TYPE);
            response = httpClient.execute(httpPost);
            if (successCode != null) {
                if (response.getStatusLine().getStatusCode() != successCode) {
                    throw new WeDPRException(
                            "send request: "
                                    + request.serialize()
                                    + " failed, status: "
                                    + response.getStatusLine().toString()
                                    + ", detail: "
                                    + EntityUtils.toString(response.getEntity()));
                }
            }
            String result = EntityUtils.toString(response.getEntity());
            logger.info(
                    "##### executePostAndGetString, request: {}, response: {}, response: {}",
                    request.serialize(),
                    result,
                    response.toString());
            return result;
        } finally {
            releaseResource(response, requestEntity);
        }
    }

    private void releaseResource(CloseableHttpResponse response, StringEntity requestEntity)
            throws Exception {
        HttpClientPool.consume(requestEntity);
        if (response != null) {
            response.close();
        }
    }

    public BaseResponse execute(String url, boolean delete) throws Exception {
        CloseableHttpResponse response = null;
        try {
            CloseableHttpClient httpClient =
                    HttpClientPool.getHttpClient(this.maxConnTotal, this.requestConfig);
            if (!delete) {
                HttpGet httpGet = new HttpGet(url);
                httpGet.setHeader(CONTENT_TYPE_KEY, DEFAULT_CONTENT_TYPE);
                response = httpClient.execute(httpGet);
            } else {
                HttpDelete httpDelete = new HttpDelete(url);
                httpDelete.setHeader(CONTENT_TYPE_KEY, DEFAULT_CONTENT_TYPE);
                response = httpClient.execute(httpDelete);
            }
            return factory.build(EntityUtils.toString(response.getEntity()));
        } finally {
            releaseResource(response, null);
        }
    }
}
