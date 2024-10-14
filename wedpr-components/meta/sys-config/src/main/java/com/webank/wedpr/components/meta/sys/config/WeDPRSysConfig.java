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

package com.webank.wedpr.components.meta.sys.config;

import com.webank.wedpr.common.protocol.SysConfigKey;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.meta.sys.config.dao.SysConfigDO;
import com.webank.wedpr.components.meta.sys.config.dao.SysConfigMapper;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class WeDPRSysConfig {
    private static final Logger logger = LoggerFactory.getLogger(WeDPRSysConfig.class);

    private SysConfigMapper sysConfigMapper;

    public WeDPRSysConfig(SysConfigMapper sysConfigMapper) {
        this.sysConfigMapper = sysConfigMapper;
        logger.info("WeDPRSysConfig, init for key: {}", SysConfigKey.getResourceLogIndexKey());
        allocateKey(SysConfigKey.getResourceLogIndexKey(), "0");
        logger.info(
                "WeDPRSysConfig, init for key: {} success", SysConfigKey.getResourceLogIndexKey());

        logger.info("WeDPRSysConfig, init for key: {}", SysConfigKey.getSyncedBlockNumber());
        // Note: set to -1 and 0 means sync event from the latest block; set to >0 means sync event
        // from the given fromBlock
        allocateKey(SysConfigKey.getSyncedBlockNumber(), "1");
        logger.info(
                "WeDPRSysConfig, init for key: {} success", SysConfigKey.getSyncedBlockNumber());
    }

    public void allocateKey(String key, String defaultValue) {
        // query the existence
        SysConfigDO result = this.sysConfigMapper.queryConfig(key);
        if (result != null) {
            logger.info("allocateKey return directly for the key {} already exists!", key);
            return;
        }
        logger.info("allocateKey for {}, value: {}", key, defaultValue);
        SysConfigDO sysConfigDO = new SysConfigDO();
        sysConfigDO.setConfigKey(key);
        sysConfigDO.setConfigValue(defaultValue);
        this.sysConfigMapper.insertConfig(sysConfigDO);
    }

    public SysConfigMapper getSysConfigMapper() {
        return this.sysConfigMapper;
    }

    @SneakyThrows(WeDPRException.class)
    public SysConfigDO getConfig(String configKey) {
        SysConfigDO config = this.sysConfigMapper.queryConfig(configKey);
        if (config == null) {
            throw new WeDPRException("The sys-config " + configKey + " doesn't exist!");
        }
        return config;
    }

    public void updateConfig(String key, String configValue) {
        SysConfigDO config = new SysConfigDO();
        config.setConfigKey(key);
        config.setConfigValue(configValue);
        this.sysConfigMapper.updateConfig(config);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean refreshConfig(String configKey, String configValue) {
        SysConfigDO sysConfigDO = new SysConfigDO();
        sysConfigDO.setConfigKey(configKey);
        sysConfigDO.setConfigValue(configValue);
        int ret = this.sysConfigMapper.refresh(sysConfigDO);
        logger.debug("refreshConfig for {}, changed-record: {}", sysConfigDO.toString(), ret);
        return (ret > 0);
    }

    public boolean resetConfig(String configKey, String configValue, long expireTimeSeconds) {
        SysConfigDO sysConfigDO = new SysConfigDO();
        sysConfigDO.setConfigKey(configKey);
        sysConfigDO.setConfigValue(configValue);
        return (this.sysConfigMapper.tryReset(sysConfigDO, expireTimeSeconds) > 0);
    }

    public boolean configExpired(String configKey, long expireTimeSeconds) {
        return Long.compare(
                        this.sysConfigMapper.getUpdateEclipsedTime(configKey), expireTimeSeconds)
                > 0;
    }

    public SysConfigDO getActiveConfig(String configKey, long expireTimeSeconds) {
        return this.sysConfigMapper.queryActiveConfig(configKey, expireTimeSeconds);
    }
}
