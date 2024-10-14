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

package com.webank.wedpr.components.sync.service.impl;

import com.github.pagehelper.PageInfo;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.mybatis.PageHelperWrapper;
import com.webank.wedpr.components.sync.dao.ResourceActionDO;
import com.webank.wedpr.components.sync.dao.SyncStatusMapper;
import com.webank.wedpr.components.sync.service.RecordSyncStatusRequest;
import com.webank.wedpr.components.sync.service.SyncService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SyncServiceImpl implements SyncService {
    private static final Logger logger = LoggerFactory.getLogger(SyncServiceImpl.class);

    @Autowired private SyncStatusMapper syncStatusMapper;

    @Override
    public WeDPRResponse queryRecordSyncStatus(RecordSyncStatusRequest request) {
        WeDPRResponse response = new WeDPRResponse();
        ResourceStatusResult resourceStatusResult = new ResourceStatusResult();
        try (PageHelperWrapper pageHelperWrapper = new PageHelperWrapper(request)) {
            List<ResourceActionDO> result =
                    syncStatusMapper.queryResourceRecordMeta(request.getResourceActionDO());
            resourceStatusResult.setTotal(new PageInfo<ResourceActionDO>(result).getTotal());
            resourceStatusResult.setDataList(result);
            response.setCode(Constant.WEDPR_SUCCESS);
            response.setMsg(Constant.WEDPR_SUCCESS_MSG);
            response.setData(resourceStatusResult);
        } catch (Exception e) {
            logger.warn(
                    "queryRecordSyncStatus exception, request: {}, error: ", request.toString(), e);
            response.setCode(Constant.WEDPR_FAILED);
            response.setMsg(
                    "queryRecordSyncStatus failed for: "
                            + e.getMessage()
                            + ", query condition: "
                            + request.toString());
        }
        return response;
    }
}
