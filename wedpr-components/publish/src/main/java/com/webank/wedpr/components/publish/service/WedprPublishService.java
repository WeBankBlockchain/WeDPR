package com.webank.wedpr.components.publish.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.webank.wedpr.components.publish.entity.WedprPublish;
import com.webank.wedpr.components.publish.entity.request.PublishRequest;
import com.webank.wedpr.components.publish.entity.request.PublishSearchRequest;
import com.webank.wedpr.components.publish.sync.PublishSyncAction;
import com.webank.wedpr.core.utils.WeDPRException;
import com.webank.wedpr.core.utils.WeDPRResponse;

/**
 * 服务类
 *
 * @author zachma
 * @since 2024-08-28
 */
public interface WedprPublishService extends IService<WedprPublish> {
    WeDPRResponse createPublishService(String username, PublishRequest request)
            throws WeDPRException;

    void syncPublishService(PublishSyncAction action, WedprPublish wedprPublish)
            throws WeDPRException;

    WeDPRResponse updatePublishService(String username, PublishRequest request);

    WeDPRResponse revokePublishService(String username, String publishId);

    WeDPRResponse listPublishService(PublishSearchRequest request);

    WeDPRResponse searchPublishService(String publishId);
}
