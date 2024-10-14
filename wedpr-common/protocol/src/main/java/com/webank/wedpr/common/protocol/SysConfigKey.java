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

package com.webank.wedpr.common.protocol;

import com.webank.wedpr.common.config.WeDPRConfig;

public class SysConfigKey {
    private static final String RESOURCE_LOG_INDEX_KEY =
            WeDPRConfig.apply("wedpr.sync.sys.config.log_index_key", "wedpr_resource_log_index");

    private static final String SYNCED_BLOCK_NUMBER =
            WeDPRConfig.apply(
                    "wedpr.sync.sys.config.synced_block_number_key", "wedpr_synced_block_number");

    public static String getResourceLogIndexKey() {
        return RESOURCE_LOG_INDEX_KEY;
    }

    public static String getSyncedBlockNumber() {
        return SYNCED_BLOCK_NUMBER;
    }
}
