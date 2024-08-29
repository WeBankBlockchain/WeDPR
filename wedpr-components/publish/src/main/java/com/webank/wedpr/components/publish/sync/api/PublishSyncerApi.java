package com.webank.wedpr.components.publish.sync.api;

import com.webank.wedpr.components.publish.sync.PublishSyncAction;

/**
 * @author zachma
 * @date 2024/8/28
 */
public interface PublishSyncerApi {
    void sync(PublishSyncAction action, String resourceContent);

    void publishSync(String resourceContent);

    void revokeSync(String resourceContent);
}
