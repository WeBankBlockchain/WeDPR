package com.webank.wedpr.components.dataset.sync;

import com.webank.wedpr.common.config.WeDPRCommonConfig;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.dataset.sync.handler.*;
import com.webank.wedpr.components.db.mapper.dataset.dao.UserInfo;
import com.webank.wedpr.components.db.mapper.dataset.mapper.wapper.DatasetTransactionalWrapper;
import com.webank.wedpr.components.sync.ResourceSyncer;
import com.webank.wedpr.components.sync.ResourceSyncer.CommitArgs;
import com.webank.wedpr.components.sync.core.ResourceActionRecord;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("datasetSyncerCommitHandler")
public class DatasetSyncerCommitHandler implements ResourceSyncer.CommitHandler {

    private static final Logger logger = LoggerFactory.getLogger(DatasetSyncerCommitHandler.class);

    @Autowired private DatasetTransactionalWrapper datasetTransactionalWrapper;

    private final Map<String, ActionHandler> actionHandlerMap = new HashMap<>();

    public DatasetSyncerCommitHandler() {
        actionHandlerMap.put(DatasetSyncerAction.CREATE.getAction(), new CreateActionHandler());
        actionHandlerMap.put(DatasetSyncerAction.REMOVE.getAction(), new RemoveActionHandler());
        actionHandlerMap.put(DatasetSyncerAction.UPDATE.getAction(), new UpdateActionHandler());
        // NOTE: add more action handler
    }

    ActionHandler getActionHandler(String action) {
        return actionHandlerMap.get(action);
    }

    @Override
    public void call(CommitArgs args) throws WeDPRException {

        long startTimeMillis = System.currentTimeMillis();

        ResourceActionRecord resourceActionRecord = args.getResourceActionRecord();
        String user = resourceActionRecord.getUser();
        String agency = resourceActionRecord.getAgency();
        String action = resourceActionRecord.getResourceAction();
        String content = resourceActionRecord.getResourceContent();

        UserInfo userInfo = UserInfo.builder().user(user).groupInfos(null).agency(agency).build();
        String myAgency = WeDPRCommonConfig.getAgency();

        logger.info(
                "receive dataset sync message, id: {}, user: {}, action: {}, content: {}, myAgency: {}",
                resourceActionRecord.getResourceID(),
                userInfo,
                action,
                resourceActionRecord,
                myAgency);

        if (agency.equals(myAgency)) {
            // self message, ignore
            logger.info(
                    "ignore self agency sync message, id: {}, user: {}, action: {}, content: {}",
                    resourceActionRecord.getResourceID(),
                    userInfo,
                    action,
                    resourceActionRecord);
            return;
        }

        ActionHandlerContext context =
                ActionHandlerContext.builder()
                        .datasetTransactionalWrapper(datasetTransactionalWrapper)
                        .build();

        ActionHandler actionHandler = getActionHandler(action);
        if (actionHandler == null) {
            logger.error(
                    "unsupported dataset sync action, id: {}, user: {}, action: {}, content: {}",
                    resourceActionRecord.getResourceID(),
                    userInfo,
                    action,
                    resourceActionRecord);
            return;
        }

        try {
            actionHandler.handle(userInfo, content, context);

            long endTimeMillis = System.currentTimeMillis();
            logger.info(
                    "handle dataset sync message success, id: {}, user: {}, action: {}, content: {}, cost(ms): {}",
                    resourceActionRecord.getResourceID(),
                    userInfo,
                    action,
                    resourceActionRecord,
                    (endTimeMillis - startTimeMillis));
        } catch (Exception e) {
            long endTimeMillis = System.currentTimeMillis();
            logger.error(
                    "handle dataset sync message exception, id: {}, user: {}, action: {}, content: {}, cost(ms): {}, e: ",
                    resourceActionRecord.getResourceID(),
                    userInfo,
                    action,
                    resourceActionRecord,
                    (endTimeMillis - startTimeMillis),
                    e);
        }
    }
}
