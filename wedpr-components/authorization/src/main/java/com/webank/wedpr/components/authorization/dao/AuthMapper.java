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

package com.webank.wedpr.components.authorization.dao;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuthMapper {

    // batch insert the auth-list
    public int batchInsertAuthList(@Param("authList") List<AuthorizationDO> authList);

    // batch update the auth-list(Note: the not-changed field should be null or "")
    public int batchUpdateAuth(@Param("authList") List<AuthorizationDO> authList);

    // query the auth meta list according to given condition(Note: only need to set the condition
    // field)
    public List<AuthorizationDO> queryAuthMetaList(
            @Param("condition") AuthorizationDO condition,
            @Param("authStatusList") List<String> authStatusList);

    // query the auth details according to given condition(Note: only need to set the condition
    // field)
    public AuthorizationDO queryAuthDetail(
            @Param("authID") String authID, @Param("applyTypeList") List<String> applyTypeList);

    public List<String> queryAuthByCondition(
            @Param("condition") AuthorizationDO condition,
            @Param("applyTypeList") List<String> applyTypeList);

    // batch insert the auth-template
    public int insertAuthTemplates(
            @Param("templateList") List<AuthorizationTemplateDO> templateDOList);

    // batch update the auth-template (Note: the not-changed field should be null or "")
    public int updateAuthTemplates(
            @Param("templateList") List<AuthorizationTemplateDO> templateList);

    // batch delete the auth-templates according to id
    public int deleteAuthTemplates(
            @Param("user") String user, @Param("templates") List<String> templates);

    // batch query the template-meta-list according to the condition (Note: only need to set the
    // condition field)
    public List<AuthorizationTemplateDO> queryAuthTemplateMetaList(
            @Param("condition") AuthorizationTemplateDO condition);

    // query the auth-template details according to the condition(Note: only need to set the
    // condition field)
    public List<AuthorizationTemplateDO> queryAuthTemplateDetails(
            @Param("templates") List<String> templates);
}
