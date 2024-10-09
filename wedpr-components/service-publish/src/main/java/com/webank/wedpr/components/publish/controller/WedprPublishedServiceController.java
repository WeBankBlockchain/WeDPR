package com.webank.wedpr.components.publish.controller;

import com.webank.wedpr.components.publish.entity.request.PublishCreateRequest;
import com.webank.wedpr.components.publish.entity.request.PublishSearchRequest;
import com.webank.wedpr.components.publish.service.WedprPublishedServiceService;
import com.webank.wedpr.components.token.auth.TokenUtils;
import com.webank.wedpr.core.config.WeDPRCommonConfig;
import com.webank.wedpr.core.utils.Constant;
import com.webank.wedpr.core.utils.WeDPRResponse;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
        path = Constant.WEDPR_API_PREFIX + "/publish",
        produces = {"application/json"})
@Slf4j
public class WedprPublishedServiceController {
    private static final Logger logger =
            LoggerFactory.getLogger(WedprPublishedServiceController.class);

    @Autowired private WedprPublishedServiceService wedprPublishService;

    @PostMapping("/create")
    public WeDPRResponse createPublish(
            @RequestBody PublishCreateRequest publishCreate, HttpServletRequest request) {
        try {
            return wedprPublishService.createPublishService(
                    TokenUtils.getLoginUser(request).getUsername(), publishCreate);
        } catch (Exception e) {
            logger.warn("发布服务失败 error: ", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, "发布服务失败 error: " + e.getMessage());
        }
    }

    @PostMapping("/update")
    public WeDPRResponse updatePublish(
            @RequestBody PublishCreateRequest publishRequest, HttpServletRequest request) {
        try {
            return wedprPublishService.updatePublishService(
                    TokenUtils.getLoginUser(request).getUsername(), publishRequest);
        } catch (Exception e) {
            logger.warn("更新已发布的服务 error: ", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, "撤回已发布的服务失败: " + e.getMessage());
        }
    }

    @PostMapping("/list")
    public WeDPRResponse listPublish(
            @RequestBody PublishSearchRequest request, HttpServletRequest httpServletRequest) {
        try {
            // Note: anyone can see all published service
            return wedprPublishService.listPublishService(
                    TokenUtils.getLoginUser(httpServletRequest).getUsername(),
                    WeDPRCommonConfig.getAgency(),
                    request);
        } catch (Exception e) {
            logger.warn("列出所有的已发布的服务 exception, error: ", e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "listPublish failed for " + e.getMessage());
        }
    }

    @DeleteMapping("/revoke/{serviceId}")
    public WeDPRResponse revokePublish(@PathVariable String serviceId, HttpServletRequest request) {
        try {
            return wedprPublishService.revokePublishService(
                    TokenUtils.getLoginUser(request).getUsername(), serviceId);
        } catch (Exception e) {
            logger.warn("撤回已发布的服务 error: ", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, "撤回已发布的服务失败: " + e.getMessage());
        }
    }
}
