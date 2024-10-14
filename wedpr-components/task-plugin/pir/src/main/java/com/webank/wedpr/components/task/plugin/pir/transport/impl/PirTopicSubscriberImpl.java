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

import com.webank.wedpr.components.api.credential.core.CredentialVerifier;
import com.webank.wedpr.components.api.credential.core.impl.CredentialInfo;
import com.webank.wedpr.components.db.mapper.service.publish.model.ServiceInvokeStatus;
import com.webank.wedpr.components.pir.sdk.config.PirSDKConfig;
import com.webank.wedpr.components.pir.sdk.core.PirMsgErrorCallback;
import com.webank.wedpr.components.pir.sdk.model.PirQueryRequest;
import com.webank.wedpr.components.task.plugin.pir.handler.PirServiceHook;
import com.webank.wedpr.components.task.plugin.pir.transport.PirTopicSubscriber;
import com.webank.wedpr.core.utils.Constant;
import com.webank.wedpr.core.utils.ThreadPoolService;
import com.webank.wedpr.core.utils.WeDPRException;
import com.webank.wedpr.core.utils.WeDPRResponse;
import com.webank.wedpr.sdk.jni.transport.IMessage;
import com.webank.wedpr.sdk.jni.transport.WeDPRTransport;
import com.webank.wedpr.sdk.jni.transport.handlers.MessageDispatcherCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PirTopicSubscriberImpl implements PirTopicSubscriber {
    private static final Logger logger = LoggerFactory.getLogger(PirTopicSubscriberImpl.class);
    private final WeDPRTransport transport;
    private final CredentialVerifier credentialVerifier;
    private final PirServiceHook pirServiceHook;
    private final ThreadPoolService threadPoolService = PirSDKConfig.getThreadPoolService();

    public PirTopicSubscriberImpl(
            WeDPRTransport transport,
            CredentialVerifier credentialVerifier,
            PirServiceHook pirServiceHook) {
        this.transport = transport;
        this.credentialVerifier = credentialVerifier;
        this.pirServiceHook = pirServiceHook;
    }

    protected void verify(CredentialInfo credentialInfo) throws Exception {
        if (credentialInfo == null) {
            throw new WeDPRException("Must define the auth information");
        }
        // verify the credential information
        credentialVerifier.verify(credentialInfo);
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
                                                PirQueryRequest request = null;
                                                try {
                                                    logger.trace(
                                                            "Receive message, service: {}, msg: {}",
                                                            serviceID,
                                                            message.toString());
                                                    request =
                                                            PirQueryRequest.deserialize(
                                                                    message.getPayload());
                                                    verify(
                                                            request.getQueryParam()
                                                                    .getCredentialInfo());
                                                    response = queryHandler.onQuery(request);
                                                    pirServiceHook.onInvoke(
                                                            request,
                                                            ServiceInvokeStatus.InvokeSuccess);
                                                } catch (Exception e) {
                                                    pirServiceHook.onInvoke(
                                                            request,
                                                            ServiceInvokeStatus.InvokeFailed);

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
