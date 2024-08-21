package com.webank.wedpr.components.dataset.sync.handler;

import com.webank.wedpr.components.dataset.dao.UserInfo;
import com.webank.wedpr.components.dataset.exception.DatasetException;

public interface ActionHandler {
    void handle(UserInfo userInfo, String content, ActionHandlerContext context)
            throws DatasetException;
}
