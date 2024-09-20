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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.webank.wedpr.core.utils.ObjectMapperFactory;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.util.StringUtils;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JupyterSetting {
    private String jupyterBinary = JupyterConfig.getDefaultJupyterBinary();
    private String configPath = JupyterConfig.getDefaultJupyterConfigPath();
    private String listenIp = JupyterConfig.getDefaultJupyterListenIp();
    private Integer listenPort;
    private String noteBookPath;

    public JupyterSetting() {}

    public JupyterSetting(String userName, Integer listenPort) {
        this.listenPort = listenPort;
        this.noteBookPath = JupyterConfig.getUserJupyterProjectPath(userName);
    }

    @SneakyThrows(Exception.class)
    public String serialize() {
        return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
    }

    @SneakyThrows(Exception.class)
    public static JupyterSetting deserialize(String data) {
        if (StringUtils.isEmpty(data)) {
            return null;
        }
        return ObjectMapperFactory.getObjectMapper().readValue(data, JupyterSetting.class);
    }
}
