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

import com.webank.wedpr.common.config.WeDPRConfig;

public class WeDPRSyncConfig {
    // address for contract ResourceLogRecordFactory.sol
    private static final String RESOURCE_LOG_RECORD_FACTORY_CONTRACT_ADDRESS =
            WeDPRConfig.apply("wedpr.sync.recorder.factory.contract_address", null, Boolean.TRUE);

    private static final String RESOURCE_SEQUENCER_CONTRACT_ADDRESS =
            WeDPRConfig.apply("wedpr.sync.sequencer.contract_address", null, Boolean.TRUE);

    private static final Integer RESOURCE_RECORD_CONTRACT_VERSION =
            WeDPRConfig.apply("wedpr.sync.recorder.contract_version", 1);

    private static final Integer RESOURCE_SYNC_QUEUE_LIMIT =
            WeDPRConfig.apply("wedpr.sync.queue_limit", 100000);

    private static final Integer WORKER_IDLE_WAIT_MS =
            WeDPRConfig.apply("wedpr.sync.worker_idle_ms", 10);

    private static final Integer SYNC_PIPLINE_SIZE =
            WeDPRConfig.apply("wedpr.sync.pipline_size", 50);

    public static String getResourceSequencerContractAddress() {
        return RESOURCE_SEQUENCER_CONTRACT_ADDRESS;
    }

    public static String getResourceLogRecordFactoryContractAddress() {
        return RESOURCE_LOG_RECORD_FACTORY_CONTRACT_ADDRESS;
    }

    public static Integer getResourceRecordContractVersion() {
        return RESOURCE_RECORD_CONTRACT_VERSION;
    }

    public static Integer getResourceSyncQueueLimit() {
        return RESOURCE_SYNC_QUEUE_LIMIT;
    }

    public static Integer getWorkerIdleWaitMs() {
        return WORKER_IDLE_WAIT_MS;
    }

    public static Integer getSyncPiplineSize() {
        return SYNC_PIPLINE_SIZE;
    }
}
