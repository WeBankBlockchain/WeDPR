package com.webank.wedpr.components.publish.service;

import com.webank.wedpr.components.db.mapper.service.publish.dao.PublishedServiceInfo;
import com.webank.wedpr.components.publish.entity.request.PublishCreateRequest;
import com.webank.wedpr.components.publish.entity.request.PublishSearchRequest;
import com.webank.wedpr.components.publish.sync.PublishSyncAction;
import com.webank.wedpr.core.utils.WeDPRResponse;

/**
 * 服务类
 *
 * @author caryliao
 * @since 2024-08-31
 */
public interface WedprPublishedServiceService {
    WeDPRResponse createPublishService(String username, PublishCreateRequest publishCreate)
            throws Exception;

    WeDPRResponse updatePublishService(String username, PublishedServiceInfo publishedServiceInfo)
            throws Exception;

    WeDPRResponse revokePublishService(String username, String serviceId) throws Exception;

    void syncPublishService(PublishSyncAction action, PublishedServiceInfo publishedServiceInfo);

    WeDPRResponse listPublishService(PublishSearchRequest request);
}
