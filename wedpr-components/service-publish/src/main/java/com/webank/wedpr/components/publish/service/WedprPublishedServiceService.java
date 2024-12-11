package com.webank.wedpr.components.publish.service;

import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.db.mapper.service.publish.dao.PublishedServiceInfo;
import com.webank.wedpr.components.publish.entity.request.PublishCreateRequest;
import com.webank.wedpr.components.publish.entity.request.PublishSearchRequest;

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

    WeDPRResponse listPublishService(String user, String agency, PublishSearchRequest request);
}
