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

package com.webank.wedpr.components.meta.sys.config.dao;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysConfigMapper {

    // insert config
    public void insertConfig(@Param("sysConfig") SysConfigDO sysConfig);

    // batch insert the sys-config
    public void batchInsertConfig(@Param("sysConfigList") List<SysConfigDO> sysConfigList);

    // update the config
    public void updateConfig(@Param("sysConfig") SysConfigDO sysConfig);

    // delete the config
    public void deleteConfig(@Param("configKey") String configKey);
    // batch delete
    public void batchDeleteConfigs(@Param("configKeyList") List<String> configKeyList);

    // query the config
    public SysConfigDO queryConfig(@Param("configKey") String configKey);

    // query all the configs
    public List<SysConfigDO> queryAllConfig();

    // refresh the last_update_time
    public int refresh(@Param("sysConfigDO") SysConfigDO sysConfigDO);

    // try to reset the config when the config has not been refreshed after given  expireTimeSeconds
    public int tryReset(
            @Param("sysConfigDO") SysConfigDO sysConfigDO,
            @Param("expireTimeSeconds") long expireTimeSeconds);

    public Long getUpdateEclipsedTime(@Param("configKey") String configKey);
    // query the config not expired
    public SysConfigDO queryActiveConfig(
            @Param("configKey") String configKey,
            @Param("expireTimeSeconds") long expireTimeSeconds);

    public Long getConfigCount();
}
