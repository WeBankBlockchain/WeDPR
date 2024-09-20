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

package com.webank.wedpr.components.integration.jupyter.demo;

import com.webank.wedpr.components.integration.jupyter.core.JupyterHostSetting;
import java.util.ArrayList;
import java.util.List;

public class GenerateJupyterHostSetting {
    public static void main(String[] args) throws Exception {
        JupyterHostSetting jupyterHostSetting = new JupyterHostSetting();
        JupyterHostSetting.SingleHostSetting singleHostSetting =
                new JupyterHostSetting.SingleHostSetting();
        // wedpr-0004
        singleHostSetting.setEntryPoint("192.168.0.238");
        singleHostSetting.setJupyterStartPort(14000);
        singleHostSetting.setMaxJupyterCount(5);

        List<JupyterHostSetting.SingleHostSetting> hosts = new ArrayList<>();
        hosts.add(singleHostSetting);
        jupyterHostSetting.setHostSettings(hosts);
        System.out.println("##### the hostSetting for jupyter: " + jupyterHostSetting.serialize());
    }
}
