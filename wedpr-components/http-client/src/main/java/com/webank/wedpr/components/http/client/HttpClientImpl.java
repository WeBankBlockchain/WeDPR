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

import com.webank.wedpr.common.utils.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
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
        this.url = Common.getUrl(url);
        this.maxConnTotal = maxConnTotal;
        this.requestConfig = requestConfig;
        this.factory = factory;
    }

    public String getUrl() {
        return this.url;
    }

    public BaseResponse executePost(BaseRequest request) throws Exception {
        return executePost(request, null);
    }

    public BaseResponse executePost(BaseRequest request, Integer successCode) throws Exception {
        return factory.build(executePostAndGetString(request, successCode));
    }

    public String executePostAndGetString(String url, String request, Integer successCode)
            throws Exception {
        StringEntity requestEntity = null;
        CloseableHttpResponse response = null;
        try {
            CloseableHttpClient httpClient =
                    HttpClientPool.getHttpClient(this.maxConnTotal, this.requestConfig);
            HttpPost httpPost = new HttpPost(url);
            requestEntity = new StringEntity(request);
            httpPost.setEntity(requestEntity);
            httpPost.setHeader(CONTENT_TYPE_KEY, DEFAULT_CONTENT_TYPE);
            response = httpClient.execute(httpPost);

            if (successCode != null) {
                if (response.getStatusLine().getStatusCode() != successCode) {
                    throw new WeDPRException(
                            "send request: "
                                    + request
                                    + " failed, status: "
                                    + response.getStatusLine().toString()
                                    + ", detail: "
                                    + EntityUtils.toString(response.getEntity()));
                }
            }

            String result = EntityUtils.toString(response.getEntity());

            logger.info(
                    "##### executePostAndGetString, request: {}, response: {}, result: {}",
                    httpPost,
                    response,
                    result);

            return result;
        } finally {
            releaseResource(response, requestEntity);
        }
    }

    public String executePostAndGetString(String request, Integer successCode) throws Exception {
        return executePostAndGetString(this.url, request, successCode);
    }

    public String executePostAndGetString(BaseRequest request, Integer successCode)
            throws Exception {
        String strRequest = request.serialize();
        return executePostAndGetString(strRequest, successCode);
    }

    private void releaseResource(CloseableHttpResponse response, StringEntity requestEntity)
            throws Exception {
        HttpClientPool.consume(requestEntity);
        if (response != null) {
            response.close();
        }
    }

    public String executeAndGetString(HttpRequestBase httpRequestBase) throws Exception {
        CloseableHttpResponse response = null;
        try {
            CloseableHttpClient httpClient =
                    HttpClientPool.getHttpClient(this.maxConnTotal, this.requestConfig);

            httpRequestBase.setHeader(CONTENT_TYPE_KEY, DEFAULT_CONTENT_TYPE);
            response = httpClient.execute(httpRequestBase);

            return EntityUtils.toString(response.getEntity());
        } finally {
            releaseResource(response, null);
        }
    }

    public BaseResponse execute(HttpRequestBase httpRequestBase) throws Exception {
        CloseableHttpResponse response = null;
        try {
            CloseableHttpClient httpClient =
                    HttpClientPool.getHttpClient(this.maxConnTotal, this.requestConfig);

            httpRequestBase.setHeader(CONTENT_TYPE_KEY, DEFAULT_CONTENT_TYPE);
            response = httpClient.execute(httpRequestBase);

            String strResponse = EntityUtils.toString(response.getEntity());

            logger.info(
                    "##### execute, request: {}, response: {}, result: {}",
                    httpRequestBase,
                    response,
                    strResponse);

            return factory.build(strResponse);
        } finally {
            releaseResource(response, null);
        }
    }

    public BaseResponse execute(String url, boolean delete) throws Exception {
        HttpRequestBase httpRequestBase = null;
        if (delete) {
            httpRequestBase = new HttpDelete(url);
        } else {
            httpRequestBase = new HttpGet(url);
        }

        return execute(httpRequestBase);
    }
}
