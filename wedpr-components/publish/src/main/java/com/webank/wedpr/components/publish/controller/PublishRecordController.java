package com.webank.wedpr.components.publish.controller;

import com.webank.wedpr.components.publish.entity.request.NodeInvokeRequest;
import com.webank.wedpr.components.publish.entity.request.PublishRecordRequest;
import com.webank.wedpr.components.publish.service.WedprPublishRecordService;
import com.webank.wedpr.core.utils.Constant;
import com.webank.wedpr.core.utils.WeDPRResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zachma
 * @date 2024/8/29
 */
@RestController
@RequestMapping(
        path = Constant.WEDPR_API_PREFIX + "/publish/record",
        produces = {"application/json"})
@Slf4j
public class PublishRecordController {
    private static final Logger logger = LoggerFactory.getLogger(PublishRecordController.class);

    @Autowired private WedprPublishRecordService wedprPublishRecordService;

    @GetMapping("/search/{publishId}")
    public WeDPRResponse searchPublishRecord(
            @Required @PathVariable String publishId, PublishRecordRequest publishRecordRequest) {
        try {
            return wedprPublishRecordService.seachPublishRecordService(
                    publishId, publishRecordRequest);
        } catch (Exception e) {
            logger.warn("发起方搜索申报记录 exception, error: ", e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "searchPublishRecord failed for " + e.getMessage());
        }
    }

    @PostMapping("/invoke")
    public WeDPRResponse invokePublishRecord(NodeInvokeRequest nodeInvokeRequest) {
        try {
            return wedprPublishRecordService.invokePublishRecordService(nodeInvokeRequest);
        } catch (Exception e) {
            logger.warn("任务结果申报失败 exception, error: ", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, "任务结果申报失败 for " + e.getMessage());
        }
    }
}
