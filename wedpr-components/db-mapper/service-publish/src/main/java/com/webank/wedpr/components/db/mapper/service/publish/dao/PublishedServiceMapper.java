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

package com.webank.wedpr.components.db.mapper.service.publish.dao;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PublishedServiceMapper {
    public List<PublishedServiceInfo> queryPublishedService(
            @Param("condition") PublishedServiceInfo condition,
            @Param("serviceIdList") List<String> serviceIdList);

    public Integer insertServiceInfo(@Param("serviceInfo") PublishedServiceInfo serviceInfo);

    public Integer updateServiceInfo(@Param("updatedInfo") PublishedServiceInfo updatedInfo);

    public Integer deleteServiceInfo(
            @Param("serviceId") String serviceId,
            @Param("owner") String owner,
            @Param("agency") String agency);
}
