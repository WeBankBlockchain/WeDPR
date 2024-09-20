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

import org.apache.commons.lang3.StringUtils;

public enum JupyterStatus {
    Allocating("Allocating"),
    Ready("Ready"),
    Running("Running"),
    OpenFailed("OpenFailed"),
    Closed("Closed");

    private final String status;

    JupyterStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }

    public static JupyterStatus deserialize(String status) {
        if (StringUtils.isBlank(status)) {
            return null;
        }
        for (JupyterStatus jupyterStatus : JupyterStatus.values()) {
            if (jupyterStatus.status.compareToIgnoreCase(status) == 0) {
                return jupyterStatus;
            }
        }
        return null;
    }

    public boolean isRunning() {
        return ordinal() == JupyterStatus.Running.ordinal();
    }

    public boolean isClosed() {
        return ordinal() == JupyterStatus.Closed.ordinal();
    }
}
