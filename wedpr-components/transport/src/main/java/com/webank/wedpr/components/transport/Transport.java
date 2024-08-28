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

package com.webank.wedpr.components.transport;

import com.webank.wedpr.components.transport.model.Message;
import com.webank.wedpr.core.utils.WeDPRException;

public interface Transport {
    // register component

    /**
     * register the component
     *
     * @param component the component used to router
     * @throws Exception failed case
     */
    void registerComponent(String component) throws Exception;

    /**
     * unregister the component
     *
     * @param component the component used to route
     * @throws Exception failed case
     */
    void unRegisterComponent(String component) throws Exception;

    /**
     * register the topic
     *
     * @param topic the topic used to route
     * @throws Exception failed case
     */
    void registerTopic(String topic) throws Exception;

    /**
     * unRegister the topic
     *
     * @param topic the topic used to route
     * @throws Exception failed case
     */
    void unRegisterTopic(String topic) throws Exception;

    public interface MessageHandler {
        void call(Message msg) throws WeDPRException;
    }

    //// the async interfaces
    /**
     * register the message handler
     *
     * @param topic the topic
     * @param messageHandler the message handler
     */
    void registerTopicHandler(String topic, MessageHandler messageHandler);

    /**
     * async send message by the nodeID
     *
     * @param topic the topic
     * @param dstNode the dstNode
     * @param payload the payload
     * @param seq the seq of the payload
     * @param timeout the timeout setting
     * @param handler the handler called after receive the message related to the topic
     */
    void asyncSendMessageByNodeID(
            String topic,
            byte[] dstNode,
            byte[] payload,
            int seq,
            long timeout,
            MessageHandler handler);

    /**
     * send message by the agency
     *
     * @param topic the topic
     * @param agency the agency
     * @param payload the payload
     * @param seq the seq
     * @param timeout the timeout
     * @param handler the handler called after receive the message related to the topic
     */
    void asyncSendMessageByAgency(
            String topic,
            String agency,
            byte[] payload,
            int seq,
            long timeout,
            MessageHandler handler);

    void asyncSendMessageByComponent(
            String topic,
            String component,
            byte[] payload,
            int seq,
            long timeout,
            MessageHandler handler);

    /**
     * send message by the topic(will register firstly)
     *
     * @param topic the topic(used to route too
     * @param payload the payload(the payload)
     * @param seq the seq(the seq)
     * @param timeout the timeout
     * @param handler the handler
     */
    void asyncSendMessageByTopic(
            String topic, byte[] payload, int seq, long timeout, MessageHandler handler);

    /** @param topic the topic to remove */
    void removeTopic(String topic);

    //// the sync interfaces
    void pushByNodeID(String topic, byte[] dstNodeID, int seq, byte[] payload) throws Exception;

    void pushByComponent(String topic, String component, int seq, byte[] payload) throws Exception;

    void pushByInst(String topic, String dstInst, int seq, byte[] payload) throws Exception;

    Message pop(String topic, long timeout);

    Message peek(String topic);
}
