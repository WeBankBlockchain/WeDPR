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

import com.webank.wedpr.common.utils.Worker;
import com.webank.wedpr.components.sync.core.ResourceActionRecord;
import com.webank.wedpr.components.sync.impl.generated.v1.ResourceLogRecord;
import com.webank.wedpr.components.sync.impl.generated.v1.ResourceLogRecordFactory;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.codec.ContractCodec;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.v3.model.EventLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceSyncEventHandler extends Worker {
    private static final Logger logger = LoggerFactory.getLogger(ResourceSyncEventHandler.class);
    private final SyncWorker syncWorker;

    private final ContractCodec contractCodec;
    private final Client bcosClient;
    // thread-safe
    // Note: the eventLog if small here, so not limit the queue size
    private final PriorityBlockingQueue<EventLog> eventLogQueue =
            new PriorityBlockingQueue<>(
                    WeDPRSyncConfig.getResourceSyncQueueLimit(),
                    new Comparator<EventLog>() {
                        @Override
                        public int compare(EventLog o1, EventLog o2) {
                            int result = o1.getBlockNumber().compareTo(o2.getBlockNumber());
                            if (result != 0) {
                                return result;
                            }
                            return o1.getTransactionIndex().compareTo(o2.getTransactionIndex());
                        }
                    });

    public ResourceSyncEventHandler(
            String workerName,
            SyncWorker syncWorker,
            ContractCodec contractCodec,
            Client bcosClient) {
        super(workerName, WeDPRSyncConfig.getWorkerIdleWaitMs());
        this.syncWorker = syncWorker;
        this.contractCodec = contractCodec;
        this.bcosClient = bcosClient;
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
        for (EventLog log : logs) {
            pushEventLog(eventSubId, status, log);
        }
    }

    private void pushEventLog(String eventSubId, int status, EventLog log) {
        try {
            this.eventLogQueue.put(log);
            logger.info(
                    "pushEventLog to queue, transactionInde: {}, status: {}, blockNumber: {}",
                    log.getTransactionIndex(),
                    status,
                    log.getBlockNumber());
        } catch (Exception e) {
            logger.error(
                    "pushEventLog to queue failed, subId: {}, status: {}, log: {}, error: ",
                    eventSubId,
                    status,
                    log.toString(),
                    e);
        }
    }

    protected boolean shouldParseEventLog() {
        return this.syncWorker.getUnHandledResourceRecordSize()
                <= WeDPRSyncConfig.getSyncPiplineSize();
    }
    // Note: since getRecord from the blockchain is time-consuming, we should parseEventLog in a
    // thread otherwise the threadPool, in case of the threadPool is blocked
    protected void parseEventLog() {
        if (this.eventLogQueue.isEmpty()) {
            return;
        }
        if (!shouldParseEventLog()) {
            return;
        }
        logger.info("parseEventLog ...");
        Integer parsedNum = 0;
        while (!eventLogQueue.isEmpty() && shouldParseEventLog()) {
            EventLog log = this.eventLogQueue.poll();
            try {
                // decode the eventLog
                SyncEventItem syncEventItem =
                        new SyncEventItem(
                                this.contractCodec.decodeEvent(
                                        ResourceLogRecordFactory.getABI(),
                                        ResourceLogRecordFactory.ADDRECORDEVENT_EVENT.getName(),
                                        log));

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
                parsedNum++;
            } catch (Exception e) {
                logger.warn("parseEventLog exception, eventLog: {},  e: ", log.toString(), e);
                break;
            }
        }
        logger.info("parseEventLog success, size: {}", parsedNum);
    }

    @Override
    protected void execute() {
        parseEventLog();
    }
}
