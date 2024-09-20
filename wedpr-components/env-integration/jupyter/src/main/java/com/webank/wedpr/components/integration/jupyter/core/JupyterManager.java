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

import com.webank.wedpr.components.integration.jupyter.client.JupyterClient;
import com.webank.wedpr.components.integration.jupyter.client.impl.JupyterClientImpl;
import com.webank.wedpr.components.integration.jupyter.dao.JupyterInfoDO;
import com.webank.wedpr.components.integration.jupyter.dao.JupyterMapper;
import com.webank.wedpr.components.meta.sys.config.dao.SysConfigMapper;
import com.webank.wedpr.core.config.WeDPRCommonConfig;
import com.webank.wedpr.core.protocol.task.TaskResponse;
import com.webank.wedpr.core.utils.WeDPRException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JupyterManager {
    private static final Logger logger = LoggerFactory.getLogger(JupyterManager.class);
    private final SysConfigMapper sysConfigMapper;
    private final JupyterMapper jupyterMapper;
    private final JupyterClient jupyterClient;

    public JupyterManager(SysConfigMapper sysConfigMapper, JupyterMapper jupyterMapper) {
        this.sysConfigMapper = sysConfigMapper;
        this.jupyterMapper = jupyterMapper;
        this.jupyterClient = new JupyterClientImpl(this.sysConfigMapper);
    }

    protected JupyterHostSetting fetchJupyterHostSettings() throws Exception {
        JupyterHostSetting jupyterHostSetting =
                JupyterHostSetting.deserialize(
                        this.sysConfigMapper
                                .queryConfig(JupyterConfig.getJupyterHostConfigurationKey())
                                .getConfigValue());
        if (jupyterHostSetting == null) {
            return null;
        }
        jupyterHostSetting.setJupyterMapper(jupyterMapper);
        return jupyterHostSetting;
    }

    protected List<JupyterInfoDO> queryJupyter(String user, String agency, String id) {
        JupyterInfoDO condition = new JupyterInfoDO(true);
        condition.setOwner(user);
        condition.setAgency(agency);
        condition.setId(id);
        return jupyterMapper.queryJupyterInfos(condition);
    }

    protected void updateJupyterStatus(String id, JupyterStatus status) {
        JupyterInfoDO updatedInfo = new JupyterInfoDO(id);
        updatedInfo.setStatus(status.getStatus());
        jupyterMapper.updateJupyterInfo(updatedInfo);
    }

    // destroy the jupyter for given person
    public synchronized Integer deleteJupyter(String user, String agency, String jupyterID) {
        try {
            List<JupyterInfoDO> jupyterList = queryJupyter(user, agency, jupyterID);
            if (jupyterList == null || jupyterList.isEmpty()) {
                logger.info(
                        "deleteJupyter return directly for no jupyter found for the user, user: {}, agency: {}, jupyterID: {}",
                        user,
                        agency,
                        jupyterID);
                return 0;
            }
            // stop the jupyter
            JupyterInfoDO stoppedJupyter = jupyterList.get(0);
            TaskResponse response = this.jupyterClient.stop(stoppedJupyter);
            logger.info(
                    "stopJupyter success, user: {}, agency: {}, jupyter: {}, response: {}",
                    user,
                    agency,
                    stoppedJupyter.toString(),
                    response.toString());

            logger.info("deleteJupyter, user: {}, jupyterID: {}", user, jupyterID);
            return this.jupyterMapper.deleteJupyterInfo(jupyterID, user);
        } catch (Exception e) {
            logger.warn(
                    "stopJupyter failed, user: {}, agency: {}, jupyter: {}, error: ",
                    user,
                    agency,
                    jupyterID,
                    e);
            return 0;
        }
    }

    protected JupyterInfoDO checkJupyterExistence(String user, String jupyterID) throws Exception {
        List<JupyterInfoDO> result = queryJupyter(user, WeDPRCommonConfig.getAgency(), jupyterID);
        if (result == null || result.isEmpty()) {
            throw new WeDPRException("The jupyter" + jupyterID + " not belongs to user " + user);
        }
        return result.get(0);
    }

    public JupyterInfoDO openJupyter(String user, String jupyterID) throws Exception {
        JupyterInfoDO result = checkJupyterExistence(user, jupyterID);
        // the jupyter is already in running status
        if (result.getJupyterStatus() != null && result.getJupyterStatus().isRunning()) {
            logger.info("the jupyter is already running, id: {}", jupyterID);
            return result;
        }
        this.jupyterClient.start(result);
        // update the status to running
        updateJupyterStatus(jupyterID, JupyterStatus.Running);
        result.setJupyterStatus(JupyterStatus.Running);
        return result;
    }

    public JupyterInfoDO closeJupyter(String user, String jupyterID) throws Exception {
        JupyterInfoDO result = checkJupyterExistence(user, jupyterID);
        // the jupyter is already in closed status
        if (result.getJupyterStatus() != null && result.getJupyterStatus().isClosed()) {
            logger.info("the jupyter is already closed, id: {}", jupyterID);
            return result;
        }
        this.jupyterClient.stop(result);
        // update the jupyter to closed
        updateJupyterStatus(jupyterID, JupyterStatus.Closed);
        result.setJupyterStatus(JupyterStatus.Closed);
        return result;
    }

    // allocate the jupyter for given person
    public synchronized String allocateJupyter(String user, String agency) throws Exception {
        // check the user has jupyter or not
        if (!queryJupyter(user, agency, null).isEmpty()) {
            throw new WeDPRException(
                    "User "
                            + user
                            + " has already allocated the jupyter, one user can only occupy one jupyter-notebook!");
        }
        // try to allocate the jupyter for new user
        JupyterHostSetting jupyterHostSetting = fetchJupyterHostSettings();
        if (jupyterHostSetting == null) {
            throw new WeDPRException("No jupyter resource now!");
        }
        // try to obtain the jupyter
        // Note: allocateJupyter will throw exception when allocate failed
        JupyterInfoDO allocatedJupyter = jupyterHostSetting.allocateJupyter(user, agency);
        this.jupyterClient.create(allocatedJupyter);
        logger.info(
                "Allocate the jupyter success, make it as ready, jupyterInfo: {}",
                allocatedJupyter.toString());
        updateJupyterStatus(allocatedJupyter.getId(), JupyterStatus.Ready);
        return allocatedJupyter.getId();
    }
}
