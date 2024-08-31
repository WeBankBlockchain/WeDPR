package com.webank.wedpr.components.publish.controller;

import com.webank.wedpr.components.publish.entity.request.PublishRequest;
import com.webank.wedpr.components.publish.entity.request.PublishSearchRequest;
import com.webank.wedpr.components.publish.service.WedprPublishService;
import com.webank.wedpr.components.token.auth.TokenUtils;
import com.webank.wedpr.core.utils.Constant;
import com.webank.wedpr.core.utils.WeDPRResponse;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zachma
 * @date 2024/8/26
 */
@RestController
@RequestMapping(
        path = Constant.WEDPR_API_PREFIX + "/publish",
        produces = {"application/json"})
@Slf4j
public class PublishServiceController {
    private static final Logger logger = LoggerFactory.getLogger(PublishServiceController.class);

    @Autowired private WedprPublishService wedprPublishService;

    @PostMapping("/create")
    public WeDPRResponse createPublish(
            @RequestBody PublishRequest publishCreate, HttpServletRequest request) {
        try {
            return wedprPublishService.createPublishService(
                    TokenUtils.getLoginUser(request).getUsername(), publishCreate);
        } catch (Exception e) {
            logger.warn("发布服务失败 error: ", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, "发布服务失败 error: " + e.getMessage());
        }
    }

    @DeleteMapping("/revoke/{publishId}")
    public WeDPRResponse revokePublish(
            @Required @PathVariable String publishId, HttpServletRequest request) {
        try {
            return wedprPublishService.revokePublishService(
                    TokenUtils.getLoginUser(request).getUsername(), publishId);
        } catch (Exception e) {
            logger.warn("撤回已发布的服务 error: ", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, "撤回已发布的服务失败: " + e.getMessage());
        }
    }

    @PostMapping("/update")
    public WeDPRResponse updatePublish(
            @RequestBody PublishRequest publishRequest, HttpServletRequest request) {
        try {
            return wedprPublishService.updatePublishService(
                    TokenUtils.getLoginUser(request).getUsername(), publishRequest);
        } catch (Exception e) {
            logger.warn("更新已发布的服务 error: ", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, "撤回已发布的服务失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public WeDPRResponse listPublish(PublishSearchRequest request) {
        try {
            return wedprPublishService.listPublishService(request);
        } catch (Exception e) {
            logger.warn("列出所有的已发布的服务 exception, error: ", e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "listPublish failed for " + e.getMessage());
        }
    }

    @GetMapping("/search/{publishId}")
    public WeDPRResponse searchPublish(@Required @PathVariable String publishId) {
        try {
            return wedprPublishService.searchPublishService(publishId);
        } catch (Exception e) {
            logger.warn("searchPublish exception, error: ", e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "createProject failed for " + e.getMessage());
        }
    }
}
