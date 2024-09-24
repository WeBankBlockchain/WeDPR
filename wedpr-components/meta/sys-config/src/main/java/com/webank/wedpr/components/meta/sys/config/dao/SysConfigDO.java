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

package com.webank.wedpr.components.meta.sys.config.dao;

import com.webank.wedpr.core.utils.WeDPRException;
import org.apache.commons.lang3.StringUtils;

public class SysConfigDO {
    private String configKey;

    private String configValue;
    private Integer reportStatus;
    private String createTime;
    private String lastUpdateTime;

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public Integer getReportStatus() {
        return reportStatus;
    }

    public void setReportStatus(Integer reportStatus) {
        this.reportStatus = reportStatus;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public void check() throws WeDPRException {
        if (StringUtils.isBlank(configKey) || StringUtils.isBlank(configValue)) {
            throw new WeDPRException(
                    "Invalid SysConfig, both the key and the value should not be empty! currentConfig: "
                            + toString());
        }
    }

    @Override
    public String toString() {
        return "SysConfigDO{"
                + "configKey='"
                + configKey
                + '\''
                + ", configValue='"
                + configValue
                + '\''
                + ", reportStatus="
                + reportStatus
                + ", createTime='"
                + createTime
                + '\''
                + ", lastUpdateTime='"
                + lastUpdateTime
                + '\''
                + '}';
    }
}
