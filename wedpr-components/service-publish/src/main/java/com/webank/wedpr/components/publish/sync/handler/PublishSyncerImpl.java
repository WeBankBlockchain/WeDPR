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

package com.webank.wedpr.components.publish.sync.handler;

import com.webank.wedpr.components.db.mapper.service.publish.dao.PublishedServiceInfo;
import com.webank.wedpr.components.db.mapper.service.publish.dao.PublishedServiceMapper;
import com.webank.wedpr.components.publish.sync.PublishSyncAction;
import java.util.List;

public class PublishSyncerImpl {
    private final PublishedServiceMapper publishedServiceMapper;

    public PublishSyncerImpl(PublishedServiceMapper publishedServiceMapper) {
        this.publishedServiceMapper = publishedServiceMapper;
    }

    public void syncPublishService(
            PublishSyncAction action, PublishedServiceInfo publishedServiceInfo) {
        List<PublishedServiceInfo> existServiceList =
                this.publishedServiceMapper.queryPublishedService(publishedServiceInfo, null);
        if (action == PublishSyncAction.SYNC) {
            if (existServiceList == null || existServiceList.isEmpty()) {
                this.publishedServiceMapper.insertServiceInfo(publishedServiceInfo);
            } else {
                this.publishedServiceMapper.updateServiceInfo(publishedServiceInfo);
            }
        }

        if (action == PublishSyncAction.REVOKE) {
            this.publishedServiceMapper.deleteServiceInfo(
                    publishedServiceInfo.getServiceId(),
                    publishedServiceInfo.getOwner(),
                    publishedServiceInfo.getAgency());
        }
    }
}
