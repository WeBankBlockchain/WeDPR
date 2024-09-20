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

package com.webank.wedpr.components.integration.jupyter.service;

import com.webank.wedpr.components.integration.jupyter.dao.JupyterInfoDO;
import java.util.List;

public interface JupyterService {

    /**
     * allocate the jupyter environment for given user
     *
     * @param user the user that apply the jupyter
     * @param agency the agency of the person
     * @return success or failed
     */
    public abstract String allocate(String user, String agency) throws Exception;

    /**
     * query jupyters by condition
     *
     * @param condition
     * @return
     */
    public abstract List<JupyterInfoDO> queryJupyters(
            boolean admin, String queryUser, JupyterInfoDO condition) throws Exception;

    /**
     * open the jupyter according to given id
     *
     * @param id the jupyter id
     * @return success/failed
     */
    public abstract JupyterInfoDO open(String currentUser, String id) throws Exception;

    /**
     * close the jupyter according to given id
     *
     * @param id specify the jupyter to close
     * @return success/failed
     */
    public abstract JupyterInfoDO close(String currentUser, String id) throws Exception;

    /**
     * destroy the specified jupyter
     *
     * @param id specify the jupyter to destory
     * @return success/failed
     */
    public abstract boolean destroy(boolean admin, String currentUser, String agency, String id);
}
