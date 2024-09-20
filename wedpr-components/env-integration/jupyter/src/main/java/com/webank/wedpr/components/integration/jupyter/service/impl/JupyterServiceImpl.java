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

package com.webank.wedpr.components.integration.jupyter.service.impl;

import com.webank.wedpr.components.hook.UserHook;
import com.webank.wedpr.components.integration.jupyter.core.JupyterConfig;
import com.webank.wedpr.components.integration.jupyter.core.JupyterManager;
import com.webank.wedpr.components.integration.jupyter.dao.JupyterInfoDO;
import com.webank.wedpr.components.integration.jupyter.dao.JupyterMapper;
import com.webank.wedpr.components.integration.jupyter.hook.JupyterUserCallback;
import com.webank.wedpr.components.integration.jupyter.service.JupyterService;
import com.webank.wedpr.components.meta.sys.config.dao.SysConfigMapper;
import com.webank.wedpr.core.config.WeDPRCommonConfig;
import java.util.List;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JupyterServiceImpl implements JupyterService {
    private static final Logger logger = LoggerFactory.getLogger(JupyterServiceImpl.class);

    private @Autowired SysConfigMapper sysConfigMapper;
    private @Autowired JupyterMapper jupyterMapper;
    private @Autowired UserHook userHook;
    private JupyterManager jupyterManager;

    @PostConstruct
    public void init() {
        this.jupyterManager = new JupyterManager(sysConfigMapper, jupyterMapper);
        userHook.registerUserCallback(
                JupyterConfig.getJupyterModule(), new JupyterUserCallback(this.jupyterManager));
    }
    /**
     * allocate the jupyter environment for given user
     *
     * @param user the user that apply the jupyter
     * @param agency the agency of the person
     * @return success or failed
     */
    @Override
    public String allocate(String user, String agency) throws Exception {
        return this.jupyterManager.allocateJupyter(user, agency);
    }

    /**
     * query jupyters by condition
     *
     * @param condition
     * @return
     */
    @Override
    public List<JupyterInfoDO> queryJupyters(
            boolean admin, String queryUser, JupyterInfoDO condition) throws Exception {
        if (!admin) {
            condition.setOwner(queryUser);
            condition.setAgency(WeDPRCommonConfig.getAgency());
        }
        return jupyterMapper.queryJupyterInfos(condition);
    }

    /**
     * destroy the specified jupyter
     *
     * @param id specify the jupyter to destory
     * @return success/failed
     */
    @Override
    public boolean destroy(boolean admin, String currentUser, String agency, String id) {
        // the admin can delete all jupyter
        if (admin) {
            return jupyterManager.deleteJupyter(null, null, id) > 0;
        }
        return jupyterMapper.deleteJupyterInfo(currentUser, id) > 0;
    }

    /**
     * open the jupyter according to given id
     *
     * @param id the jupyter id
     * @return success/failed
     */
    @Override
    public JupyterInfoDO open(String currentUser, String id) throws Exception {
        return this.jupyterManager.openJupyter(currentUser, id);
    }

    /**
     * close the jupyter according to given id
     *
     * @param id specify the jupyter to close
     * @return success/failed
     */
    @Override
    public JupyterInfoDO close(String currentUser, String id) throws Exception {
        return this.jupyterManager.closeJupyter(currentUser, id);
    }
}
