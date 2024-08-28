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

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface JupyterMapper {
    /**
     * insert the jupyter record
     *
     * @param jupyterInfo the jupyter record to be inserted into
     * @return
     */
    public int insertJupyterInfo(@Param("jupyterInfo") JupyterInfoDO jupyterInfo);

    /**
     * update the jupyter record
     *
     * @param id the jupyter record that need to be updated
     * @param updatedInfo the updated info
     * @return
     */
    public int updateJupyterInfo(@Param("updatedInfo") JupyterInfoDO updatedInfo);

    /**
     * delete the jupyter record
     *
     * @param id the jupyter that need to be deleted
     * @return
     */
    public Integer deleteJupyterInfo(@Param("id") String id, @Param("owner") String owner);

    /**
     * query the jupyter information by condition
     *
     * @param condition the condition used to query
     * @return the result
     */
    public List<JupyterInfoDO> queryJupyterInfos(@Param("condition") JupyterInfoDO condition);

    public Integer queryJupyterRecordCount(@Param("condition") JupyterInfoDO condition);
}
