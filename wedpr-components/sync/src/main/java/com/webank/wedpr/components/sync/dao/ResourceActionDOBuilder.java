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

package com.webank.wedpr.components.sync.dao;

import com.webank.wedpr.components.sync.core.ResourceActionRecord;
import java.math.BigInteger;

public class ResourceActionDOBuilder {
    public static ResourceActionDO build(String status, ResourceActionRecord record) {
        ResourceActionDO resourceActionDO = new ResourceActionDO(record);
        resourceActionDO.setStatus(status);
        return resourceActionDO;
    }

    public static ResourceActionDO build(
            String status, ResourceActionRecord record, String statusMsg) {
        ResourceActionDO resourceActionDO = build(status, record);
        resourceActionDO.setStatusMsg(statusMsg);
        return resourceActionDO;
    }

    public static ResourceActionDO build(String resourceID, BigInteger blockNumber, String status) {
        ResourceActionDO resourceActionDO = new ResourceActionDO();
        resourceActionDO.setResourceID(resourceID);
        resourceActionDO.setStatus(status);
        resourceActionDO.setBlockNumber(blockNumber);
        return resourceActionDO;
    }
}
