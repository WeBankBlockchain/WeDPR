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

package com.webank.wedpr.components.integration.jupyter.hook;

import com.webank.wedpr.common.config.WeDPRCommonConfig;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.hook.UserHook;
import com.webank.wedpr.components.integration.jupyter.core.JupyterManager;
import org.apache.commons.lang3.StringUtils;

public class JupyterUserCallback implements UserHook.UserCallback {
    private final JupyterManager jupyterManager;

    public JupyterUserCallback(JupyterManager jupyterManager) {
        this.jupyterManager = jupyterManager;
    }

    @Override
    public boolean interruptOnException() {
        return false;
    }

    @Override
    public void onCreated(String user) throws Exception {
        this.jupyterManager.allocateJupyter(user, WeDPRCommonConfig.getAgency());
    }

    // do nothing
    @Override
    public void onUpdated(String user) throws Exception {}

    @Override
    public void onDeleted(String user) throws Exception {
        if (StringUtils.isBlank(user)) {
            throw new WeDPRException("Delete jupyter failed, must specify the jupyter user");
        }
        this.jupyterManager.deleteJupyter(user, WeDPRCommonConfig.getAgency(), null);
    }
}
