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

import com.webank.wedpr.components.blockchain.BlockChainBuilder;
import com.webank.wedpr.components.leader.election.LeaderElection;
import com.webank.wedpr.components.meta.sys.config.WeDPRSysConfig;
import com.webank.wedpr.components.sync.ResourceSyncer;
import com.webank.wedpr.components.sync.core.ResourceActionRecord;
import com.webank.wedpr.components.sync.dao.ResourceActionDO;
import com.webank.wedpr.components.sync.dao.ResourceActionDOBuilder;
import com.webank.wedpr.components.sync.dao.SyncStatusMapperWrapper;
import com.webank.wedpr.components.sync.impl.generated.v1.ResourceLogRecordFactory;
import com.webank.wedpr.core.protocol.SysConfigKey;
import com.webank.wedpr.core.utils.ThreadPoolService;
import com.webank.wedpr.core.utils.WeDPRException;
import com.webank.wedpr.core.utils.WeDPRResponse;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.reflection.ArrayUtil;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.v3.eventsub.EventSubCallback;
import org.fisco.bcos.sdk.v3.eventsub.EventSubParams;
import org.fisco.bcos.sdk.v3.eventsub.EventSubscribe;
import org.fisco.bcos.sdk.v3.model.EventLog;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.codec.decode.RevertMessageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockChainResourceSyncImpl extends ResourceSyncerImpl implements ResourceSyncer {
    private static final Logger logger = LoggerFactory.getLogger(BlockChainResourceSyncImpl.class);
    private final ResourceLogRecordFactory resourceLogRecordFactoryContract;
    private final ResourceSyncEventHandler resourceSyncEventHandler;
    private final EventSubscribe eventSubscribe;
    private List<String> topics = new ArrayList<>();
    private String eventID;
    private final Client blockChainClient;

    public static class WeDPRResourceSyncEventSubCallback implements EventSubCallback {
        private final ResourceSyncEventHandler resourceSyncEventHandler;

        public WeDPRResourceSyncEventSubCallback(
                ResourceSyncEventHandler resourceSyncEventHandler) {
            this.resourceSyncEventHandler = resourceSyncEventHandler;
        }

        @Override
        public void onReceiveLog(String eventSubId, int status, List<EventLog> logs) {
            this.resourceSyncEventHandler.onReceiveEventLog(eventSubId, status, logs);
        }
    }

    public static class ResourceSyncTransactionCallback extends TransactionCallback {
        private final ResourceActionRecord record;
        private final SyncStatusMapperWrapper syncStatusMapper;

        public ResourceSyncTransactionCallback(
                SyncStatusMapperWrapper syncStatusMapper, ResourceActionRecord record) {
            this.syncStatusMapper = syncStatusMapper;
            this.record = record;
        }

        @SneakyThrows(Exception.class)
        @Override
        public void onResponse(TransactionReceipt receipt) {
            if (receipt.isStatusOK()) {
                this.syncStatusMapper.setSyncRecordStatus(
                        ResourceActionDOBuilder.build(
                                record.getResourceID(),
                                receipt.getBlockNumber(),
                                ResourceRecordStatus.SUBMITTED_TO_CHAIN_SUCCESS.getStatus()));
                return;
            }
            Tuple2<Boolean, String> errorOutput =
                    RevertMessageParser.tryResolveRevertMessage(receipt);
            ResourceActionDO resourceActionDO =
                    ResourceActionDOBuilder.build(
                            record.getResourceID(),
                            receipt.getBlockNumber(),
                            ResourceRecordStatus.SUBMITTED_TO_CHAIN_FAILED.getStatus());
            resourceActionDO.setStatusMsg(
                    (new WeDPRResponse(receipt.getStatus(), errorOutput.getValue2())).serialize());
            this.syncStatusMapper.setSyncRecordStatus(resourceActionDO);
            logger.error(
                    "ResourceSyncTransactionCallback: sync resourceAction to blockchain failed, resourceAction: {}, receiptStatus: {}, receiptErrorInfo: {}",
                    this.record.toString(),
                    receipt.getStatus(),
                    errorOutput.getValue2());
        }
    }

    @SneakyThrows(Exception.class)
    public BlockChainResourceSyncImpl(
            WeDPRSysConfig weDPRSysConfig,
            LeaderElection leaderElection,
            SyncStatusMapperWrapper syncStatusMapper,
            ThreadPoolService threadPoolService) {
        super(weDPRSysConfig, leaderElection, syncStatusMapper, threadPoolService);

        this.blockChainClient = BlockChainBuilder.getClient();
        this.resourceSyncEventHandler =
                new ResourceSyncEventHandler(
                        "eventlog-parser",
                        syncWorker,
                        BlockChainBuilder.createContractCodec(blockChainClient),
                        blockChainClient);

        resourceLogRecordFactoryContract =
                ResourceLogRecordFactory.load(
                        WeDPRSyncConfig.getResourceLogRecordFactoryContractAddress(),
                        blockChainClient);
        this.eventSubscribe = BlockChainBuilder.buildEventSubscribe(blockChainClient);
        topics.add(
                BlockChainBuilder.buildEventEncoder(blockChainClient)
                        .encode(ResourceLogRecordFactory.ADDRECORDEVENT_EVENT));
        logger.info("BlockChainResourceSyncImpl, event list: {}", ArrayUtil.toString(topics));
    }

    @Override
    public void onLeaderSwitch(boolean isLeader, String leaderID) {
        if (!isLeader) {
            switchToFollower(leaderID);
        } else {
            switchToLeader(leaderID);
        }
        super.onLeaderSwitch(isLeader, leaderID);
    }

    private void switchToFollower(String leaderID) {
        logger.info("onLeaderSwitch, switch to follower, leaderID: {}", leaderID);
        if (StringUtils.isBlank(eventID)) {
            return;
        }
        logger.info("onLeaderSwitch, the follower unsubscribe topic, event: {}", eventID);
        this.eventSubscribe.unsubscribeEvent(this.eventID);
        // reset the eventID
        this.eventID = null;
    }

    private void switchToLeader(String leaderID) {
        // recover the synced block
        BigInteger blockNumber =
                new BigInteger(
                        this.syncWorker
                                .getResourceSyncStatus()
                                .getWeDPRSysConfig()
                                .getConfig(SysConfigKey.getSyncedBlockNumber())
                                .getConfigValue());
        logger.info(
                "onLeaderSwitch, switch to leader, recover the synced blockNumber, begin subscribe the ResourceActionEvent, synced block: {}",
                blockNumber);
        EventSubParams eventSubParams = new EventSubParams();
        eventSubParams.setFromBlock(blockNumber);
        eventSubParams.setToBlock(BigInteger.valueOf(-1));
        eventSubParams.addAddress(WeDPRSyncConfig.getResourceLogRecordFactoryContractAddress());
        int i = 0;
        for (String topic : topics) {
            eventSubParams.addTopic(i, topic);
            i++;
        }
        this.eventID =
                this.eventSubscribe.subscribeEvent(
                        eventSubParams,
                        new WeDPRResourceSyncEventSubCallback(resourceSyncEventHandler));
        logger.info("onLeaderSwitch, switch to leader success, event: {}", this.eventID);
    }

    @SneakyThrows(Exception.class)
    @Override
    public void sync(String trigger, ResourceActionRecord record) {
        record.setTrigger(trigger);
        ResourceActionDO resourceActionDO =
                ResourceActionDOBuilder.build(
                        ResourceRecordStatus.WAITING_SUBMIT_TO_CHAIN.getStatus(), record);
        try {
            this.syncStatusMapper.setSyncRecordStatus(resourceActionDO);
            this.resourceLogRecordFactoryContract.addRecord(
                    record.serialize(),
                    BigInteger.valueOf(WeDPRSyncConfig.getResourceRecordContractVersion()),
                    new ResourceSyncTransactionCallback(this.syncStatusMapper, record));
            resourceActionDO.setStatus(ResourceRecordStatus.SUBMITTED_TO_CHAIN.getStatus());
            this.syncStatusMapper.setSyncRecordStatus(resourceActionDO);
        } catch (Exception e) {
            logger.error(
                    "sync resource meta failed, resource-meta: {}, error: ", record.toString(), e);
            resourceActionDO.setStatus(ResourceRecordStatus.SUBMITTED_TO_CHAIN_FAILED.getStatus());
            resourceActionDO.setStatusMsg(
                    "sync resource meta failed, resourceID: "
                            + record.getResourceID()
                            + ", error: "
                            + e.getMessage());
            this.syncStatusMapper.setSyncRecordStatus(resourceActionDO);
            throw new WeDPRException(
                    "Sync resource meta " + record.toString() + " failed, error: " + e.getMessage(),
                    e);
        }
    }

    @Override
    public void start() {
        super.start();
        this.resourceSyncEventHandler.startWorking();
    }

    public void stop() {
        super.stop();
        this.resourceSyncEventHandler.stop();
        this.eventSubscribe.stop();
        this.blockChainClient.stop();
    }
}
