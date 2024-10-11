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

package com.webank.wedpr.components.task.plugin.pir.transport.impl;

import com.webank.wedpr.components.pir.sdk.config.PirSDKConfig;
import com.webank.wedpr.components.pir.sdk.core.PirMsgErrorCallback;
import com.webank.wedpr.components.pir.sdk.model.PirQueryRequest;
import com.webank.wedpr.components.task.plugin.pir.transport.PirTopicSubscriber;
import com.webank.wedpr.core.utils.Constant;
import com.webank.wedpr.core.utils.ThreadPoolService;
import com.webank.wedpr.core.utils.WeDPRResponse;
import com.webank.wedpr.sdk.jni.transport.IMessage;
import com.webank.wedpr.sdk.jni.transport.WeDPRTransport;
import com.webank.wedpr.sdk.jni.transport.handlers.MessageDispatcherCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PirTopicSubscriberImpl implements PirTopicSubscriber {
    private static final Logger logger = LoggerFactory.getLogger(PirTopicSubscriberImpl.class);
    private final WeDPRTransport transport;
    private final ThreadPoolService threadPoolService = PirSDKConfig.getThreadPoolService();

    public PirTopicSubscriberImpl(WeDPRTransport transport) {
        this.transport = transport;
    }

    @Override
    public void registerService(String serviceID, QueryHandler queryHandler) throws Exception {
        // register the component
        this.transport.registerComponent(PirSDKConfig.getPirComponent(serviceID));
        logger.info(
                "register component for service {} success, component: {}",
                serviceID,
                PirSDKConfig.getPirComponent(serviceID));
        // register the topic handler
        this.transport.registerTopicHandler(
                PirSDKConfig.getPirTopic(serviceID),
                new MessageDispatcherCallback() {
                    @Override
                    public void onMessage(IMessage message) {
                        threadPoolService
                                .getThreadPool()
                                .execute(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                WeDPRResponse response = new WeDPRResponse();
                                                try {
                                                    logger.trace(
                                                            "Receive message, service: {}, msg: {}",
                                                            serviceID,
                                                            message.toString());
                                                    PirQueryRequest request =
                                                            PirQueryRequest.deserialize(
                                                                    message.getPayload());
                                                    response = queryHandler.onQuery(request);
                                                } catch (Exception e) {
                                                    logger.error(
                                                            "Handle PirQuery failed, service: {}, error: ",
                                                            serviceID,
                                                            e);
                                                    response.setCode(Constant.WEDPR_FAILED);
                                                    response.setMsg(
                                                            "Handle PirQuery failed for "
                                                                    + e.getMessage());
                                                }
                                                transport.asyncSendResponse(
                                                        message.getHeader().getSrcNode(),
                                                        message.getHeader().getTraceID(),
                                                        response.serializeToBytes(),
                                                        0,
                                                        new PirMsgErrorCallback(
                                                                "asyncSendResponseForQuery"));
                                            }
                                        });
                    }
                });
    }
}
