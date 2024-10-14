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
package com.webank.wedpr.components.integration.jupyter.client;

import com.webank.wedpr.common.config.WeDPRCommonConfig;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.integration.jupyter.core.JupyterConfig;
import com.webank.wedpr.components.integration.jupyter.dao.JupyterInfoDO;
import java.util.HashMap;
import java.util.Map;

public interface JupyterClient {

    WeDPRResponse create(JupyterInfoDO jupyterInfo) throws Exception;

    WeDPRResponse start(JupyterInfoDO jupyterInfo) throws Exception;

    WeDPRResponse stop(JupyterInfoDO jupyterInfo) throws Exception;

    static Map<String, String> generateParamMap(JupyterInfoDO jupyterInfoDO) {
        Map<String, String> paramMap = new HashMap<>();
        // the username
        paramMap.put(WeDPRCommonConfig.getParamKeyUserName(), jupyterInfoDO.getOwner());
        // the listen ip
        paramMap.put(
                WeDPRCommonConfig.getParamKeyListenIp(),
                jupyterInfoDO.getJupyterSetting().getListenIp());
        // the listen port
        paramMap.put(
                WeDPRCommonConfig.getParamKeyListenPort(),
                String.valueOf(jupyterInfoDO.getJupyterSetting().getListenPort()));
        // the jupyter binary
        paramMap.put(
                JupyterConfig.getParamKeyJupyterBinary(),
                jupyterInfoDO.getJupyterSetting().getJupyterBinary());
        // the jupyter config path
        paramMap.put(
                JupyterConfig.getParamKeyJupyterConfigPath(),
                jupyterInfoDO.getJupyterSetting().getConfigPath());
        // the notebook project directory
        paramMap.put(
                JupyterConfig.getParamKeyJupyterProjectPath(),
                jupyterInfoDO.getJupyterSetting().getNoteBookPath());
        // the secret information
        paramMap.put(
                JupyterConfig.getParamKeyJupyterAuthSecretFilePath(),
                JupyterConfig.getJupyterAuthSecretFilePath());
        return paramMap;
    }
}
