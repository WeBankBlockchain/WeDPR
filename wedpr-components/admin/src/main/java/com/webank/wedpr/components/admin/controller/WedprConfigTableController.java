package com.webank.wedpr.components.admin.controller;

import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.meta.sys.config.service.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前端控制器
 *
 * @author caryliao
 * @since 2024-09-07
 */
@RestController
@RequestMapping(
        path = Constant.WEDPR_API_PREFIX + "/admin",
        produces = {"application/json"})
@Slf4j
public class WedprConfigTableController {
    @Autowired private SysConfigService sysConfigService;

    @GetMapping("/getConfig")
    public WeDPRResponse getSystemConfig(@RequestParam String key) {
        try {
            return sysConfigService.getSystemConfig(key);
        } catch (Exception e) {
            log.warn("getSystemConfig exception, key: {}, error: ", key, e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "getSystemConfig failed for " + e.getMessage());
        }
    }
}
