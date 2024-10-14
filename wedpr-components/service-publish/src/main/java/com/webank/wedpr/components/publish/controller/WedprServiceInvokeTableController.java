package com.webank.wedpr.components.publish.controller;

import com.webank.wedpr.common.config.WeDPRCommonConfig;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.publish.entity.request.PublishInvokeSearchRequest;
import com.webank.wedpr.components.publish.service.WedprServiceInvokeTableService;
import com.webank.wedpr.components.token.auth.TokenUtils;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前端控制器
 *
 * @author caryliao
 * @since 2024-08-31
 */
@RestController
@RequestMapping(
        path = Constant.WEDPR_API_PREFIX + "/publish/invoke",
        produces = {"application/json"})
@Slf4j
public class WedprServiceInvokeTableController {
    private static final Logger logger =
            LoggerFactory.getLogger(WedprServiceInvokeTableController.class);

    @Autowired private WedprServiceInvokeTableService wedprPublishInvokeService;

    @PostMapping("/search")
    public WeDPRResponse searchPublishRecord(
            @RequestBody PublishInvokeSearchRequest publishInvokeRequest,
            HttpServletRequest request) {
        try {
            return wedprPublishInvokeService.seachPublishInvokeService(
                    TokenUtils.getLoginUser(request).getUsername(),
                    WeDPRCommonConfig.getAgency(),
                    publishInvokeRequest);
        } catch (Exception e) {
            logger.warn("searchPublishRecord exception, error: ", e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "searchPublishRecord failed for " + e.getMessage());
        }
    }
}
