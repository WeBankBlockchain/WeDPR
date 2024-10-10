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
package com.webank.wedpr.components.sync.impl;

import com.webank.wedpr.components.sync.core.ResourceActionRecord;
import com.webank.wedpr.components.sync.impl.generated.v1.ResourceLogRecord;
import com.webank.wedpr.components.sync.impl.generated.v1.ResourceLogRecordFactory;
import com.webank.wedpr.core.utils.ThreadPoolService;
import java.math.BigInteger;
import java.util.List;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.codec.ContractCodec;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.v3.model.EventLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceSyncEventHandler {
    private static final Logger logger = LoggerFactory.getLogger(ResourceSyncEventHandler.class);
    private final SyncWorker syncWorker;
    private final ContractCodec contractCodec;
    private final ThreadPoolService threadPoolService;
    private final Client bcosClient;

    public ResourceSyncEventHandler(
            SyncWorker syncWorker,
            ContractCodec contractCodec,
            Client bcosClient,
            ThreadPoolService threadPoolService) {
        this.syncWorker = syncWorker;
        this.contractCodec = contractCodec;
        this.bcosClient = bcosClient;
        this.threadPoolService = threadPoolService;
    }

    public void onReceiveEventLog(String eventSubId, int status, List<EventLog> logs) {
        if (status < 0) {
            logger.warn(
                    "WeDPRResourceSyncEventSubCallback: receive invalid event response, eventSubId: {}, status: {}",
                    eventSubId,
                    status);
            return;
        }

        if (logs == null) {
            logger.info(
                    "WeDPRResourceSyncEventSubCallback: receive null logs response, eventSubId: {}, status: {}",
                    eventSubId,
                    status);
            return;
        }

        threadPoolService
                .getThreadPool()
                .execute(
                        () -> {
                            for (EventLog log : logs) {
                                handleEventLog(eventSubId, status, log);
                            }
                        });
    }

    private void handleEventLog(String eventSubId, int status, EventLog log) {
        try {
            // decode the eventLog
            SyncEventItem syncEventItem =
                    new SyncEventItem(
                            this.contractCodec.decodeEvent(
                                    ResourceLogRecordFactory.getABI(),
                                    ResourceLogRecordFactory.ADDRECORDEVENT_EVENT.getName(),
                                    log));
            logger.info(
                    "handleEventLog, event: {}, status: {}, syncEventItem: {}, blockNumber: {}",
                    eventSubId,
                    status,
                    syncEventItem.toString(),
                    log.getBlockNumber());
            // fetch ResourceLogRecord from blockChain
            ResourceLogRecord record =
                    ResourceLogRecord.load(syncEventItem.getRecordAddress(), this.bcosClient);
            Tuple2<BigInteger, String> recordResult = record.getRecord();
            ResourceActionRecord resourceActionRecord =
                    ResourceActionRecord.deserialize(recordResult.getValue2());
            resourceActionRecord.setIndex(recordResult.getValue1());
            resourceActionRecord.setBlockNumber(log.getBlockNumber());
            resourceActionRecord.setTransactionHash(log.getTransactionHash());
            this.syncWorker.push(resourceActionRecord);
        } catch (Exception e) {
            logger.error(
                    "handleEventLog failed, subId: {}, status: {}, log: {}, error: ",
                    eventSubId,
                    status,
                    log.toString(),
                    e);
        }
    }
}
