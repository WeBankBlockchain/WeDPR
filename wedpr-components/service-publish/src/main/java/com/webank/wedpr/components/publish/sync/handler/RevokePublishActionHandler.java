package com.webank.wedpr.components.publish.sync.handler;

import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.db.mapper.service.publish.dao.PublishedServiceInfo;
import com.webank.wedpr.components.publish.sync.PublishSyncAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zachma
 * @date 2024/8/28
 */
public class RevokePublishActionHandler implements PublishActionHandler {
    private static final Logger logger = LoggerFactory.getLogger(RevokePublishActionHandler.class);

    @Override
    public void handle(String content, PublishSyncerImpl syncer) throws WeDPRException {
        try {
            PublishedServiceInfo publishedServiceInfo = PublishedServiceInfo.deserialize(content);
            syncer.syncPublishService(PublishSyncAction.REVOKE, publishedServiceInfo);
        } catch (Exception e) {
            logger.error("sync revoke publish service failed: " + content);
            throw new WeDPRException(e);
        }
    }
}
