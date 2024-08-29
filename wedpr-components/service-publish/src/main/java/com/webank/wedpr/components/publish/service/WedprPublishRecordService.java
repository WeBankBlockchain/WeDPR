package com.webank.wedpr.components.publish.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.webank.wedpr.components.publish.entity.WedprPublishRecord;
import com.webank.wedpr.components.publish.entity.request.NodeInvokeRequest;
import com.webank.wedpr.components.publish.entity.request.PublishRecordRequest;
import com.webank.wedpr.core.utils.WeDPRResponse;

/**
 * 服务类
 *
 * @author zachma
 * @since 2024-08-28
 */
public interface WedprPublishRecordService extends IService<WedprPublishRecord> {

    WeDPRResponse seachPublishRecordService(
            String publishId, PublishRecordRequest publishRecordRequest);

    WeDPRResponse invokePublishRecordService(NodeInvokeRequest nodeInvokeRequest);
}
