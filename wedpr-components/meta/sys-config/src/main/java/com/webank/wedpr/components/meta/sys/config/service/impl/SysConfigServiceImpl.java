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

package com.webank.wedpr.components.meta.sys.config.service.impl;

import com.webank.wedpr.components.meta.sys.config.dao.SysConfigDO;
import com.webank.wedpr.components.meta.sys.config.dao.SysConfigMapper;
import com.webank.wedpr.components.meta.sys.config.service.SysConfigService;
import com.webank.wedpr.core.utils.Constant;
import com.webank.wedpr.core.utils.WeDPRResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysConfigServiceImpl implements SysConfigService {
    private static final Logger logger = LoggerFactory.getLogger(SysConfigService.class);

    @Autowired private SysConfigMapper sysConfigMapper;

    @Override
    public WeDPRResponse getSystemConfig(String key) {
        WeDPRResponse response =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        try {
            SysConfigDO sysConfigDO = this.sysConfigMapper.queryConfig(key);
            if (sysConfigDO == null) {
                return response;
            }
            response.setData(sysConfigDO.getConfigValue());
        } catch (Exception e) {
            logger.warn("getSystemConfig exception, key: {}, error: ", key, e);
            response.setCode(Constant.WEDPR_FAILED);
            response.setMsg(
                    "getSystemConfig failed for key " + key + ", reason: " + e.getMessage());
        }
        return response;
    }

    @Override
    public WeDPRResponse insertSystemConfig(SysConfigDO sysConfig) {
        WeDPRResponse response =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        try {
            sysConfig.check();
            this.sysConfigMapper.insertConfig(sysConfig);
            logger.info("insertSystemConfig success, detail: {}", sysConfig.toString());
        } catch (Exception e) {
            logger.warn(
                    "insertSystemConfig exception, detail: {}, error: ", sysConfig.toString(), e);
            response.setCode(Constant.WEDPR_FAILED);
            response.setMsg(
                    "insertSystemConfig failed for "
                            + e.getMessage()
                            + ", config detail: "
                            + sysConfig.toString());
        }
        return response;
    }

    @Override
    public WeDPRResponse updateSystemConfig(SysConfigDO sysConfigDO) {
        WeDPRResponse response =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        try {
            sysConfigDO.check();
            this.sysConfigMapper.updateConfig(sysConfigDO);
            logger.info("updateSystemConfig success, detail: {}", sysConfigDO.toString());
        } catch (Exception e) {
            logger.warn(
                    "updateSystemConfig failed, detail: {}, error: ", sysConfigDO.toString(), e);
            response.setCode(Constant.WEDPR_FAILED);
            response.setMsg(
                    "updateSystemConfig failed for "
                            + e.getMessage()
                            + ", config detail: "
                            + sysConfigDO.toString());
        }
        return response;
    }
}
