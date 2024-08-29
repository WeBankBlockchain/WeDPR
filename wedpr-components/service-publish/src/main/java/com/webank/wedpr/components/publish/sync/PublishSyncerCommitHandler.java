package com.webank.wedpr.components.publish.sync;

import com.webank.wedpr.components.publish.service.WedprPublishRecordService;
import com.webank.wedpr.components.publish.service.WedprPublishService;
import com.webank.wedpr.components.publish.sync.handler.PublishActionContext;
import com.webank.wedpr.components.publish.sync.handler.PublishActionHandler;
import com.webank.wedpr.components.publish.sync.handler.ReportPublishActionHandler;
import com.webank.wedpr.components.publish.sync.handler.RevokePublishActionHandler;
import com.webank.wedpr.components.publish.sync.handler.SyncPublishActionHandler;
import com.webank.wedpr.components.sync.ResourceSyncer;
import com.webank.wedpr.components.sync.core.ResourceActionRecord;
import com.webank.wedpr.core.config.WeDPRCommonConfig;
import com.webank.wedpr.core.utils.WeDPRException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zachma
 * @date 2024/8/28
 */
@Component("publishSyncerCommitHandler")
public class PublishSyncerCommitHandler implements ResourceSyncer.CommitHandler {
    private static final Logger logger = LoggerFactory.getLogger(PublishSyncerCommitHandler.class);

    @Autowired private WedprPublishService wedprPublishService;

    @Autowired private WedprPublishRecordService wedprPublishRecordService;

    private final Map<String, PublishActionHandler> actionHandlerMap = new HashMap<>();

    public PublishSyncerCommitHandler() {
        actionHandlerMap.put(PublishSyncAction.SYNC.getAction(), new SyncPublishActionHandler());
        actionHandlerMap.put(
                PublishSyncAction.REVOKE.getAction(), new RevokePublishActionHandler());
        actionHandlerMap.put(
                PublishSyncAction.REPORT.getAction(), new ReportPublishActionHandler());
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
        String myAgency = WeDPRCommonConfig.getAgency();

        logger.info(
                "ignore self agency sync message, id: {}, action: {}, content: {}",
                resourceActionRecord.getResourceID(),
                action,
                resourceActionRecord);

        if (agency.equals(myAgency)) {
            logger.info(
                    "ignore self agency sync message, id: {}, action: {}, content: {}",
                    resourceActionRecord.getResourceID(),
                    action,
                    resourceActionRecord);
            return;
        }

        PublishActionContext context =
                PublishActionContext.builder()
                        .wedprPublishService(wedprPublishService)
                        .wedprPublishRecordService(wedprPublishRecordService)
                        .build();

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
            publishActionHandler.handle(content, context);
        } catch (Exception e) {
            logger.error(
                    "handle dataset sync message exception, id: {}, action: {}, content: {}, e: {}",
                    resourceActionRecord.getResourceID(),
                    action,
                    resourceActionRecord,
                    e);
        }
    }
}
