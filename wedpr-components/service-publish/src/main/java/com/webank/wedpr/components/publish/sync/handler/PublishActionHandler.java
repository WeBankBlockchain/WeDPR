package com.webank.wedpr.components.publish.sync.handler;

import com.webank.wedpr.common.utils.WeDPRException;

/**
 * @author zachma
 * @date 2024/8/28
 */
public interface PublishActionHandler {
    void handle(String content, PublishActionContext context) throws WeDPRException;
}
