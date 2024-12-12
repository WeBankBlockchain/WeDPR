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

package com.webank.wedpr.components.integration.jupyter.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.integration.jupyter.dao.JupyterInfoDO;
import com.webank.wedpr.components.integration.jupyter.dao.JupyterMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
public class JupyterHostSetting {
    private static final Logger logger = LoggerFactory.getLogger(JupyterHostSetting.class);

    @Data
    public static class SingleHostSetting {
        // the client entryPoint
        private String jupyterExternalIp;
        // the entry point of the host
        private String entryPoint;
        // the limitation
        private Integer maxJupyterCount = JupyterConfig.getMaxJupyterPerHost();
        // the startPort
        private Integer jupyterStartPort = JupyterConfig.getDefaultJupyterStartPort();

        public String getJupyterExternalIp() {
            if (StringUtils.isBlank(jupyterExternalIp)) {
                if (StringUtils.isBlank(entryPoint)) {
                    return entryPoint;
                }
                return entryPoint.split(":")[0];
            }
            return jupyterExternalIp;
        }
    }

    private List<SingleHostSetting> hostSettings = new ArrayList<>();

    @JsonIgnore private JupyterMapper jupyterMapper;

    public void setJupyterMapper(JupyterMapper jupyterMapper) {
        this.jupyterMapper = jupyterMapper;
    }

    public JupyterInfoDO allocateJupyter(String userName, String agency) throws Exception {
        // query the allocated jupyter
        JupyterInfoDO condition = new JupyterInfoDO(true);
        SingleHostSetting allocatedHost = null;
        Integer listenPort = 0;
        for (SingleHostSetting host : hostSettings) {
            condition.setAccessEntry(host.getEntryPoint());
            Integer count = jupyterMapper.queryJupyterRecordCount(condition);
            Integer allocatedCount = (count != null ? count : 0);
            if (allocatedCount >= host.getMaxJupyterCount()) {
                continue;
            } else {
                allocatedHost = host;
                listenPort = allocatedHost.getJupyterStartPort() + allocatedCount;
            }
        }
        if (allocatedHost == null) {
            throw new WeDPRException("Insufficient jupyter resources!");
        }
        logger.info(
                "allocateJupyter, user: {}, agency: {}, host: {}",
                userName,
                agency,
                allocatedHost.toString());

        JupyterSetting jupyterSetting = new JupyterSetting(userName, listenPort);
        // set the host ip
        jupyterSetting.setHostIp(allocatedHost.getJupyterExternalIp());
        // insert the information
        JupyterInfoDO allocatedJupyter = new JupyterInfoDO();
        allocatedJupyter.setAgency(agency);
        allocatedJupyter.setOwner(userName);
        allocatedJupyter.setAccessEntry(allocatedHost.getEntryPoint());
        allocatedJupyter.setJupyterSetting(jupyterSetting);
        allocatedJupyter.setStatus(JupyterStatus.Allocating.getStatus());
        jupyterMapper.insertJupyterInfo(allocatedJupyter);
        return allocatedJupyter;
    }

    public void setHostSettings(List<SingleHostSetting> hostSettings) {
        if (hostSettings == null) {
            return;
        }
        this.hostSettings = hostSettings;
    }

    public String serialize() throws Exception {
        return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
    }

    public static JupyterHostSetting deserialize(String data) throws Exception {
        if (StringUtils.isBlank(data)) {
            return null;
        }
        return ObjectMapperFactory.getObjectMapper().readValue(data, JupyterHostSetting.class);
    }
}
