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

package com.webank.wedpr.components.meta.agency.service.impl;

import com.github.pagehelper.PageInfo;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.meta.agency.dao.AgencyDO;
import com.webank.wedpr.components.meta.agency.dao.AgencyMapper;
import com.webank.wedpr.components.meta.agency.model.AgencyList;
import com.webank.wedpr.components.meta.agency.model.AgencyRequest;
import com.webank.wedpr.components.meta.agency.service.AgencyService;
import com.webank.wedpr.components.mybatis.PageHelperWrapper;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AgencyServiceImpl implements AgencyService {
    private static final Logger logger = LoggerFactory.getLogger(AgencyServiceImpl.class);

    @Autowired private AgencyMapper agencyMapper;

    @Override
    public WeDPRResponse queryAgencyMetas(AgencyRequest condition) {
        WeDPRResponse response =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        try (PageHelperWrapper pageHelperWrapper = new PageHelperWrapper(condition)) {
            List<AgencyDO> agencyDOList =
                    this.agencyMapper.queryByCondition(condition.getCondition());
            long count = new PageInfo<AgencyDO>(agencyDOList).getTotal();
            response.setData(new AgencyList(count, agencyDOList));
            return response;
        } catch (Exception e) {
            logger.warn("queryAgencyMetas failed, condition: {}, error: ", condition.toString(), e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "queryAgencyMetas failed for " + e.getMessage());
        }
    }
}
