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

package com.webank.wedpr.components.publish.sync;

import com.github.pagehelper.PageInfo;
import com.webank.wedpr.components.db.mapper.service.publish.dao.PublishedServiceInfo;
import com.webank.wedpr.components.db.mapper.service.publish.dao.PublishedServiceMapper;
import com.webank.wedpr.components.db.mapper.service.publish.model.ServiceStatus;
import com.webank.wedpr.components.mybatis.PageHelperWrapper;
import com.webank.wedpr.components.publish.config.ServicePublisherConfig;
import com.webank.wedpr.components.publish.sync.api.PublishSyncerApi;
import com.webank.wedpr.core.utils.PageRequest;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@DisallowConcurrentExecution
@Slf4j
public class PublishQuartzJob implements Job {

    @Data
    protected static class ServiceInfoQueryRequest extends PageRequest {
        private PublishedServiceInfo condition = new PublishedServiceInfo();

        public ServiceInfoQueryRequest(int syncStatus, ServiceStatus serviceStatus) {
            this.condition.setSyncStatus(syncStatus);
            this.condition.setServiceStatus(serviceStatus);
        }
    };

    @Qualifier("publishSyncer")
    @Autowired
    private PublishSyncerApi publishSyncer;

    @Autowired private PublishedServiceMapper publishedServiceMapper;

    @Override
    public void execute(JobExecutionContext context) {
        log.info("PublishQuartzJob run");
        try {
            syncPublishSuccessServiceInfo();
        } catch (Throwable e) {
            log.warn("PublishQuartzJob run error", e);
        }
    }

    // Note: only sync the publishing success service
    private void syncPublishSuccessServiceInfo() {
        log.info("syncPublishSuccessServiceInfo...");
        ServiceInfoQueryRequest request =
                new ServiceInfoQueryRequest(0, ServiceStatus.PublishSuccess);
        request.setPageNum(1);
        request.setPageSize(ServicePublisherConfig.getServiceSyncerBatchSize());
        try (PageHelperWrapper pageHelperWrapper = new PageHelperWrapper(request)) {
            List<PublishedServiceInfo> resultList =
                    this.publishedServiceMapper.queryPublishedService(request.getCondition());
            if (resultList == null || resultList.isEmpty()) {
                return;
            }
            log.info(
                    "syncPublishSuccessServiceInfo, {} records",
                    new PageInfo<PublishedServiceInfo>(resultList).getTotal());
            for (PublishedServiceInfo serviceInfo : resultList) {
                this.publishSyncer.publishSync(serviceInfo.serialize());
                PublishedServiceInfo updatedServiceInfo =
                        new PublishedServiceInfo(serviceInfo.getServiceId());
                updatedServiceInfo.setSyncStatus(1);
                this.publishedServiceMapper.updateServiceInfo(updatedServiceInfo);
            }
            log.info("syncPublishSuccessServiceInfo success, records: {}", resultList.size());
        } catch (Exception e) {
            log.warn("syncPublishSuccessServiceInfo error: ", e);
        }
    }
}
