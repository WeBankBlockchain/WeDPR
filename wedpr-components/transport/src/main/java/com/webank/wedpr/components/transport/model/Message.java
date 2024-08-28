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

package com.webank.wedpr.components.transport.model;

public interface Message {
    public interface MessageHeader {
        int getVersion();

        String getTraceID();

        String getSrcGwNode();

        String getDstGwNode();

        int getPacketType();

        int getTTL();

        int getExt();

        boolean isRespPacket();

        int getRouteType();

        String getComponentType();

        byte[] getSrcNode();

        byte[] getDstNode();

        String getDstInst();

        String getTopic();
    }

    abstract MessageHeader getHeader();

    abstract byte[] getPayload();
}
