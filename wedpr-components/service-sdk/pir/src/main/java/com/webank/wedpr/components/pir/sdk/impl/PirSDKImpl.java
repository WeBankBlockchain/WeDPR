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

package com.webank.wedpr.components.pir.sdk.impl;

import com.webank.wedpr.components.pir.sdk.PirSDK;
import com.webank.wedpr.components.pir.sdk.config.PirSDKConfig;
import com.webank.wedpr.components.pir.sdk.core.ObfuscateData;
import com.webank.wedpr.components.pir.sdk.core.ObfuscateQueryResult;
import com.webank.wedpr.components.pir.sdk.core.OtCrypto;
import com.webank.wedpr.components.pir.sdk.model.PirQueryParam;
import com.webank.wedpr.components.pir.sdk.model.PirQueryRequest;
import com.webank.wedpr.components.pir.sdk.model.PirResult;
import com.webank.wedpr.core.utils.Constant;
import com.webank.wedpr.core.utils.WeDPRResponse;
import com.webank.wedpr.sdk.jni.generated.Error;
import com.webank.wedpr.sdk.jni.generated.SendResponseHandler;
import com.webank.wedpr.sdk.jni.transport.IMessage;
import com.webank.wedpr.sdk.jni.transport.WeDPRTransport;
import com.webank.wedpr.sdk.jni.transport.handlers.MessageCallback;
import com.webank.wedpr.sdk.jni.transport.handlers.MessageErrorCallback;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PirSDKImpl implements PirSDK {
    private static final Logger logger = LoggerFactory.getLogger(PirSDKImpl.class);
    private final WeDPRTransport transport;

    public PirSDKImpl(WeDPRTransport transport) {
        this.transport = transport;
    }

    @Override
    public Pair<WeDPRResponse, PirResult> query(PirQueryParam queryParam) throws Exception {
        queryParam.check();
        logger.debug("Generate the obfuscate param");
        ObfuscateData obfuscateData =
                OtCrypto.generateOtParam(queryParam.getAlgorithmType(), queryParam);
        logger.debug("Generate the obfuscate param success");
        // Note: the searchIdList is sensitive that should not been passed to the pir-service
        PirQueryParam nonSensitiveQueryParam = queryParam.clone();
        nonSensitiveQueryParam.setSearchIdList(null);
        PirQueryRequest pirQueryRequest =
                new PirQueryRequest(nonSensitiveQueryParam, obfuscateData);
        Pair<WeDPRResponse, ObfuscateQueryResult> result = submitQuery(pirQueryRequest);
        if (result.getRight() == null) {
            return new ImmutablePair<>(result.getLeft(), null);
        }
        // decrypt the result
        PirResult pirResult =
                OtCrypto.decryptAndGetResult(
                        obfuscateData, queryParam, result.getRight().getOtResultList());
        return new ImmutablePair<>(result.getLeft(), pirResult);
    }

    protected Pair<WeDPRResponse, ObfuscateQueryResult> submitQuery(
            PirQueryRequest pirQueryRequest) {
        try {
            logger.debug(
                    "submitQuery, targetService: {}",
                    pirQueryRequest.getQueryParam().getServiceId());
            CompletableFuture<WeDPRResponse> queriedResult = new CompletableFuture<>();
            // Note: the dstInst is unknown
            this.transport.asyncSendMessageByComponent(
                    PirSDKConfig.getPirTopic(pirQueryRequest.getQueryParam().getServiceId()),
                    null,
                    PirSDKConfig.getPirComponent(pirQueryRequest.getQueryParam().getServiceId()),
                    pirQueryRequest.serialize().getBytes(StandardCharsets.UTF_8),
                    0,
                    PirSDKConfig.getPirQueryTimeoutMs(),
                    new MessageErrorCallback() {
                        @Override
                        public void onErrorResult(Error error) {
                            PirSDKConfig.getThreadPoolService()
                                    .getThreadPool()
                                    .execute(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        if (error == null
                                                                || error.errorCode() == 0) {
                                                            logger.debug(
                                                                    "submitQuery: sendPirRequest success, targetService: {}",
                                                                    pirQueryRequest
                                                                            .getQueryParam()
                                                                            .getServiceId());
                                                            return;
                                                        }
                                                        logger.error(
                                                                "submitQuery: sendPirRequest failed, targetService: {}, code: {}, msg: {}",
                                                                pirQueryRequest
                                                                        .getQueryParam()
                                                                        .getServiceId(),
                                                                error.errorCode(),
                                                                error.errorMessage());
                                                        queriedResult.complete(
                                                                new WeDPRResponse(
                                                                        (int) error.errorCode(),
                                                                        error.errorMessage()));
                                                    } catch (Exception e) {
                                                        logger.warn(
                                                                "submitQuery threadPool exception, service: {}, error: ",
                                                                pirQueryRequest
                                                                        .getQueryParam()
                                                                        .getServiceId(),
                                                                e);
                                                    }
                                                }
                                            });
                        }
                    },
                    new MessageCallback() {
                        @Override
                        public void onMessage(
                                Error error,
                                IMessage message,
                                SendResponseHandler sendResponseHandler) {
                            PirSDKConfig.getThreadPoolService()
                                    .getThreadPool()
                                    .execute(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        if (error != null
                                                                && error.errorCode() != 0) {
                                                            logger.error(
                                                                    "PirQuery failed, queryParam: {}, code: {}, msg: {}",
                                                                    pirQueryRequest
                                                                            .getQueryParam()
                                                                            .toString(),
                                                                    error.errorCode(),
                                                                    error.errorMessage());
                                                            queriedResult.complete(
                                                                    new WeDPRResponse(
                                                                            (int) error.errorCode(),
                                                                            error.errorMessage()));
                                                            return;
                                                        }
                                                        logger.debug(
                                                                "PirQuery, get response from the server, msg: {}, payload: {}",
                                                                message.toString(),
                                                                new String(message.getPayload()));
                                                        queriedResult.complete(
                                                                WeDPRResponse.deserialize(
                                                                        message.getPayload()));
                                                    } catch (Exception e) {
                                                        logger.warn(
                                                                "PirQuery error, targetService: {}, parse the response message exception for ",
                                                                pirQueryRequest
                                                                        .getQueryParam()
                                                                        .getServiceId(),
                                                                e);
                                                        queriedResult.complete(
                                                                new WeDPRResponse(
                                                                        Constant.WEDPR_FAILED,
                                                                        e.getMessage()));
                                                    }
                                                }
                                            });
                        }
                    });
            WeDPRResponse queriedResponse = queriedResult.get();
            if (!queriedResponse.statusOk()) {
                return new ImmutablePair<>(queriedResponse, null);
            }
            return new ImmutablePair<>(
                    queriedResponse, ObfuscateQueryResult.deserialize(queriedResponse.getData()));
        } catch (Exception e) {
            logger.warn("submitQuery exception: ", e);
            return new ImmutablePair<>(
                    new WeDPRResponse(
                            Constant.WEDPR_FAILED, "submitQuery exception for " + e.getMessage()),
                    null);
        }
    }
}
