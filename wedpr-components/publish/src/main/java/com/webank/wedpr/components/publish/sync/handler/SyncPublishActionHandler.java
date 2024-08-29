package com.webank.wedpr.components.publish.sync.handler;

import com.webank.wedpr.components.publish.entity.WedprPublish;
import com.webank.wedpr.components.publish.service.WedprPublishService;
import com.webank.wedpr.components.publish.sync.PublishSyncAction;
import com.webank.wedpr.components.publish.utils.JsonUtils;
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
            WedprPublish wedprPublish = JsonUtils.jsonString2Object(content, WedprPublish.class);
            WedprPublishService wedprPublishService = context.getWedprPublishService();
            wedprPublishService.syncPublishService(PublishSyncAction.SYNC, wedprPublish);
        } catch (Exception e) {
            logger.error("sync publish service failed: " + content);
            throw new WeDPRException(e);
        }
    }
}
