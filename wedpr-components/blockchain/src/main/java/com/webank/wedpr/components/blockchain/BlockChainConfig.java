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
package com.webank.wedpr.components.blockchain;

import com.webank.wedpr.common.config.WeDPRConfig;
import com.webank.wedpr.common.utils.Constant;

public class BlockChainConfig {
    // the group-id
    private static final String GROUP_ID =
            WeDPRConfig.apply("wedpr.chain.group_id", "", Boolean.TRUE);
    private static final String CHAIN_CONFIG_PATH =
            WeDPRConfig.apply("wedpr.chain.config_path", Constant.CHAIN_CONFIG_FILE);

    public static String getGroupId() {
        return GROUP_ID;
    }

    public static String getChainConfigPath() {
        return CHAIN_CONFIG_PATH;
    }
}
