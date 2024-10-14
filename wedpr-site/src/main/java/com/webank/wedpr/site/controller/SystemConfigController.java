/*
 * Copyright 2017-2025  [webank-wedpr]
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.webank.wedpr.site.controller;

import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.meta.sys.config.dao.SysConfigDO;
import com.webank.wedpr.components.meta.sys.config.service.SysConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        path = Constant.WEDPR_API_PREFIX + "/config/",
        produces = {"application/json"})
public class SystemConfigController {
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationController.class);

    @Autowired private SysConfigService sysConfigService;

    @GetMapping("/getConfig")
    public WeDPRResponse getSystemConfig(@RequestParam String key) {
        try {
            return sysConfigService.getSystemConfig(key);
        } catch (Exception e) {
            logger.warn("getSystemConfig exception, key: {}, error: ", key, e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "getSystemConfig failed for " + e.getMessage());
        }
    }

    @PostMapping("/insertConfig")
    public WeDPRResponse insertSystemConfig(@RequestBody SysConfigDO sysConfig) {
        try {
            return sysConfigService.insertSystemConfig(sysConfig);
        } catch (Exception e) {
            logger.warn("insertSystemConfig failed, config: {}, error: ", sysConfig.toString(), e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "insertSystemConfig failed for " + e.getMessage());
        }
    }

    @PostMapping("/updateConfig")
    public WeDPRResponse updateSystemConfig(@RequestBody SysConfigDO sysConfig) {
        try {
            return sysConfigService.updateSystemConfig(sysConfig);
        } catch (Exception e) {
            logger.warn("updateSystemConfig failed, config: {}, error: ", sysConfig.toString(), e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "updateSystemConfig failed for " + e.getMessage());
        }
    }
}
