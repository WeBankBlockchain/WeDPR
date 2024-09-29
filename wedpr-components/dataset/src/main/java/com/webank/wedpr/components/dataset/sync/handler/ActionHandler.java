package com.webank.wedpr.components.dataset.sync.handler;

import com.webank.wedpr.components.db.mapper.dataset.dao.UserInfo;
import com.webank.wedpr.components.db.mapper.dataset.exception.DatasetException;

public interface ActionHandler {
    void handle(UserInfo userInfo, String content, ActionHandlerContext context)
            throws DatasetException;
}
