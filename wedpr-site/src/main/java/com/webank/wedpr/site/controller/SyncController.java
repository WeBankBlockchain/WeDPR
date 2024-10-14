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

package com.webank.wedpr.site.controller;

import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.sync.service.RecordSyncStatusRequest;
import com.webank.wedpr.components.sync.service.SyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        path = Constant.WEDPR_API_PREFIX + "/sync/",
        produces = {"application/json"})
public class SyncController {
    private static final Logger logger = LoggerFactory.getLogger(SyncController.class);
    @Autowired private SyncService syncService;

    @PostMapping("/queryRecordSyncStatus")
    public WeDPRResponse queryRecordSyncStatus(@RequestBody RecordSyncStatusRequest condition) {
        try {
            return this.syncService.queryRecordSyncStatus(condition);
        } catch (Exception e) {
            logger.warn(
                    "queryRecordSyncStatus exception, condition: {}, error: ",
                    condition.toString(),
                    e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "queryRecordSyncStatus failed for " + e.getMessage());
        }
    }
}
