package com.webank.wedpr.components.dataset.sync.handler;

import com.webank.wedpr.components.db.mapper.dataset.mapper.wapper.DatasetTransactionalWrapper;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActionHandlerContext {
    private DatasetTransactionalWrapper datasetTransactionalWrapper;
}
