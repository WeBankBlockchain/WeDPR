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

package com.webank.wedpr.site.controller;

import com.webank.wedpr.common.config.WeDPRCommonConfig;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.scheduler.SchedulerService;
import com.webank.wedpr.components.scheduler.impl.JobDetailRequest;
import com.webank.wedpr.components.token.auth.TokenUtils;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        path = Constant.WEDPR_API_PREFIX + "/scheduler",
        produces = {"application/json"})
public class SchedulerController {
    private static final Logger logger = LoggerFactory.getLogger(SchedulerController.class);

    @Autowired private SchedulerService schedulerService;

    // create the authorization request
    @PostMapping("/queryJobDetail")
    public WeDPRResponse queryJobDetail(
            @RequestBody JobDetailRequest jobDetailRequest, HttpServletRequest request) {
        try {
            WeDPRResponse response =
                    new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);

            response.setData(
                    this.schedulerService.queryJobDetail(
                            TokenUtils.getLoginUser(request).getUsername(),
                            WeDPRCommonConfig.getAgency(),
                            jobDetailRequest));
            return response;
        } catch (Exception e) {
            logger.warn(
                    "queryJobDetail exception, job: {}, error: ", jobDetailRequest.getJobID(), e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED,
                    "queryJobDetail for job "
                            + jobDetailRequest.getJobID()
                            + " failed for "
                            + e.getMessage());
        }
    }
}
