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

package com.webank.wedpr.components.integration.jupyter.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.webank.wedpr.components.integration.jupyter.core.JupyterStatus;
import com.webank.wedpr.components.uuid.generator.WeDPRUuidGenerator;
import com.webank.wedpr.core.utils.TimeRange;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString
@EqualsAndHashCode(callSuper = false)
public class JupyterInfoDO extends TimeRange {
    private String id = WeDPRUuidGenerator.generateID();
    private String owner;
    private String agency;
    private String accessEntry;
    private String setting;
    private String status;
    private String createTime;
    private String lastUpdateTime;
    @JsonIgnore private JupyterStatus jupyterStatus;

    public JupyterInfoDO() {}

    public JupyterInfoDO(boolean resetID) {
        if (resetID) {
            setId(null);
        }
    }

    public JupyterInfoDO(String id) {
        setId(id);
    }

    public void setStatus(String status) {
        this.status = status;
        if (this.status.isEmpty()) {
            return;
        }
        this.jupyterStatus = JupyterStatus.deserialize(status);
    }

    public void setJupyterStatus(JupyterStatus jupyterStatus) {
        this.jupyterStatus = jupyterStatus;
        if (jupyterStatus == null) {
            return;
        }
        this.status = jupyterStatus.getStatus();
    }
}
