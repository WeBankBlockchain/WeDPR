package com.webank.wedpr.components.publish.sync;

import com.webank.wedpr.common.config.WeDPRCommonConfig;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.db.mapper.service.publish.dao.PublishedServiceMapper;
import com.webank.wedpr.components.publish.sync.handler.PublishActionHandler;
import com.webank.wedpr.components.publish.sync.handler.PublishSyncerImpl;
import com.webank.wedpr.components.publish.sync.handler.RevokePublishActionHandler;
import com.webank.wedpr.components.publish.sync.handler.SyncPublishActionHandler;
import com.webank.wedpr.components.sync.ResourceSyncer;
import com.webank.wedpr.components.sync.core.ResourceActionRecord;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zachma
 * @date 2024/8/28
 */
public class PublishSyncerCommitHandler implements ResourceSyncer.CommitHandler {
    private static final Logger logger = LoggerFactory.getLogger(PublishSyncerCommitHandler.class);

    private PublishedServiceMapper publishedServiceMapper;

    private final PublishSyncerImpl syncer;
    private final Map<String, PublishActionHandler> actionHandlerMap = new HashMap<>();

    public PublishSyncerCommitHandler(PublishedServiceMapper publishedServiceMapper) {
        this.publishedServiceMapper = publishedServiceMapper;
        this.syncer = new PublishSyncerImpl(publishedServiceMapper);
        actionHandlerMap.put(PublishSyncAction.SYNC.getAction(), new SyncPublishActionHandler());
        actionHandlerMap.put(
                PublishSyncAction.REVOKE.getAction(), new RevokePublishActionHandler());
    }

    PublishActionHandler getActionHandler(String action) {
        return actionHandlerMap.get(action);
    }

    @Override
    public void call(ResourceSyncer.CommitArgs args) throws WeDPRException {
        ResourceActionRecord resourceActionRecord = args.getResourceActionRecord();
        String agency = resourceActionRecord.getAgency();
        String action = resourceActionRecord.getResourceAction();
        String content = resourceActionRecord.getResourceContent();
        if (agency.equalsIgnoreCase(WeDPRCommonConfig.getAgency())) {
            logger.info(
                    "ignore self agency sync message, id: {}, action: {}, content: {}",
                    resourceActionRecord.getResourceID(),
                    action,
                    resourceActionRecord);
            return;
        }
        PublishActionHandler publishActionHandler = getActionHandler(action);
        if (publishActionHandler == null) {
            logger.error(
                    "unsupported publish sync action, id: {}, action: {}, content: {}",
                    resourceActionRecord.getResourceID(),
                    action,
                    resourceActionRecord);
            return;
        }

        try {
            publishActionHandler.handle(content, syncer);
        } catch (Exception e) {
            logger.error(
                    "handle service publish sync message exception, id: {}, action: {}, content: {}, e: ",
                    resourceActionRecord.getResourceID(),
                    action,
                    resourceActionRecord,
                    e);
        }
    }
}
