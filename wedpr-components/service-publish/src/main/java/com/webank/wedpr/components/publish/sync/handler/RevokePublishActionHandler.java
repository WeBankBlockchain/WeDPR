package com.webank.wedpr.components.publish.sync.handler;

<<<<<<< HEAD
import com.webank.wedpr.components.publish.entity.WedprPublishService;
=======
import com.webank.wedpr.components.publish.entity.WedprPublish;
import com.webank.wedpr.components.publish.service.WedprPublishService;
>>>>>>> c0ce0d5bc82d8ee00533f54c5ed4ad108ea0cd96
import com.webank.wedpr.components.publish.sync.PublishSyncAction;
import com.webank.wedpr.components.publish.utils.JsonUtils;
import com.webank.wedpr.core.utils.WeDPRException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zachma
 * @date 2024/8/28
 */
public class RevokePublishActionHandler implements PublishActionHandler {
    private static final Logger logger = LoggerFactory.getLogger(RevokePublishActionHandler.class);

    @Override
    public void handle(String content, PublishActionContext context) throws WeDPRException {
        try {
<<<<<<< HEAD
            WedprPublishService wedprPublish = JsonUtils.jsonString2Object(content, WedprPublishService.class);
            com.webank.wedpr.components.publish.service.WedprPublishService wedprPublishService = context.getWedprPublishService();
=======
            WedprPublish wedprPublish = JsonUtils.jsonString2Object(content, WedprPublish.class);
            WedprPublishService wedprPublishService = context.getWedprPublishService();
>>>>>>> c0ce0d5bc82d8ee00533f54c5ed4ad108ea0cd96
            wedprPublishService.syncPublishService(PublishSyncAction.REVOKE, wedprPublish);
        } catch (Exception e) {
            logger.error("sync revoke publish service failed: " + content);
            throw new WeDPRException(e);
        }
    }
}
