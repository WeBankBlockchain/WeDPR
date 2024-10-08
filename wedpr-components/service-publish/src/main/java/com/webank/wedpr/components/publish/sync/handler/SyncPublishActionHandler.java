package com.webank.wedpr.components.publish.sync.handler;

import com.webank.wedpr.components.db.mapper.service.publish.dao.PublishedServiceInfo;
import com.webank.wedpr.components.publish.service.WedprPublishedServiceService;
import com.webank.wedpr.components.publish.sync.PublishSyncAction;
import com.webank.wedpr.core.utils.WeDPRException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zachma
 * @date 2024/8/28
 */
public class SyncPublishActionHandler implements PublishActionHandler {
    private static final Logger logger = LoggerFactory.getLogger(SyncPublishActionHandler.class);

    @Override
    public void handle(String content, PublishActionContext context) throws WeDPRException {
        try {
            PublishedServiceInfo publishedServiceInfo = PublishedServiceInfo.deserialize(content);
            WedprPublishedServiceService wedprPublishService = context.getWedprPublishedService();
            wedprPublishService.syncPublishService(PublishSyncAction.SYNC, publishedServiceInfo);
        } catch (Exception e) {
            logger.error("sync publish service failed: " + content);
            throw new WeDPRException(e);
        }
    }
}
