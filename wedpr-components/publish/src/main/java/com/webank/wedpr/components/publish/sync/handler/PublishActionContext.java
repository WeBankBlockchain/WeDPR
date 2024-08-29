package com.webank.wedpr.components.publish.sync.handler;

import com.webank.wedpr.components.publish.service.WedprPublishRecordService;
import com.webank.wedpr.components.publish.service.WedprPublishService;
import lombok.Builder;
import lombok.Data;

/**
 * @author zachma
 * @date 2024/8/28
 */
@Data
@Builder
public class PublishActionContext {
    private WedprPublishService wedprPublishService;

    private WedprPublishRecordService wedprPublishRecordService;
}
