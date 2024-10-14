package com.webank.wedpr.components.publish.sync;

import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.components.publish.sync.api.PublishSyncerApi;
import com.webank.wedpr.components.sync.ResourceSyncer;
import com.webank.wedpr.components.sync.core.ResourceActionRecord;
import com.webank.wedpr.components.sync.core.ResourceActionRecorderBuilder;
import com.webank.wedpr.components.uuid.generator.WeDPRUuidGenerator;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zachma
 * @date 2024/8/28
 */
@Data
public class PublishSyncer implements PublishSyncerApi {
    private static final Logger logger = LoggerFactory.getLogger(PublishSyncer.class);
    private ResourceSyncer resourceSyncer;
    private ResourceActionRecorderBuilder resourceBuilder;

    @Override
    public void sync(PublishSyncAction action, String resourceContent) {
        String syncId = WeDPRUuidGenerator.generateID();
        ResourceActionRecord resourceActionRecord =
                this.resourceBuilder.build(syncId, action.getAction(), resourceContent);
        this.resourceSyncer.sync(Constant.SYS_USER, resourceActionRecord);
        logger.info(
                "sync publish, syncId: {}, action: {}, content: {}",
                syncId,
                action,
                resourceActionRecord);
    }

    @Override
    public void publishSync(String resourceContent) {
        sync(PublishSyncAction.SYNC, resourceContent);
    }

    @Override
    public void revokeSync(String resourceContent) {
        sync(PublishSyncAction.REVOKE, resourceContent);
    }
}
