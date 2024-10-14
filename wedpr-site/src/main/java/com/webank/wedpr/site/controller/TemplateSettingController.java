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

import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.meta.setting.template.model.TemplateSettingQueryRequest;
import com.webank.wedpr.components.meta.setting.template.model.TemplateSettingRequest;
import com.webank.wedpr.components.meta.setting.template.service.TemplateSettingService;
import com.webank.wedpr.components.token.auth.TokenUtils;
import com.webank.wedpr.components.token.auth.model.UserToken;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.ibatis.reflection.ArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        path = Constant.WEDPR_API_PREFIX + "/setting/",
        produces = {"application/json"})
public class TemplateSettingController {
    private final Logger logger = LoggerFactory.getLogger(TemplateSettingController.class);

    @Autowired private TemplateSettingService templateSettingService;

    @PostMapping("/insertSettings")
    public WeDPRResponse batchInsertTemplateSettings(
            @RequestBody TemplateSettingRequest settings, HttpServletRequest request) {
        try {
            UserToken userToken = TokenUtils.getLoginUser(request);
            return this.templateSettingService.batchInsertTemplateSettings(
                    userToken.isAdmin(), userToken.getUsername(), settings);
        } catch (Exception e) {
            logger.warn("insertSettings exception, error: ", e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "insertSettings failed, error: " + e.getMessage());
        }
    }

    @PostMapping("/updateSettings")
    public WeDPRResponse batchUpdateTemplateSettings(
            @RequestBody TemplateSettingRequest settings, HttpServletRequest request) {
        try {
            UserToken userToken = TokenUtils.getLoginUser(request);
            return this.templateSettingService.batchUpdateTemplateSettings(
                    userToken.isAdmin(), userToken.getUsername(), settings);
        } catch (Exception e) {
            logger.warn("updateSettings exception, error: ", e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "updateSettings failed, error: " + e.getMessage());
        }
    }

    @PostMapping("/deleteSettings")
    public WeDPRResponse deleteTemplateSettings(
            @RequestBody List<String> templateIDList, HttpServletRequest request) {
        try {
            UserToken userToken = TokenUtils.getLoginUser(request);
            return this.templateSettingService.deleteTemplateSettings(
                    userToken.isAdmin(), userToken.getUsername(), templateIDList);
        } catch (Exception e) {
            logger.warn(
                    "deleteTemplateSettings exception, templateIDs:{}, error: ",
                    ArrayUtil.toString(templateIDList),
                    e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED,
                    "deleteTemplateSettings failed, error: " + e.getMessage());
        }
    }

    @PostMapping("/querySettings")
    public WeDPRResponse querySettings(
            @RequestBody TemplateSettingQueryRequest queryRequest, HttpServletRequest request) {
        try {
            UserToken userToken = TokenUtils.getLoginUser(request);
            return this.templateSettingService.querySettings(
                    userToken.isAdmin(), userToken.getUsername(), queryRequest);
        } catch (Exception e) {
            logger.warn(
                    "querySettings exception, condition:{}, onlyMeta: {}, error: ",
                    queryRequest.getCondition().toString(),
                    queryRequest.getOnlyMeta(),
                    e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED,
                    "querySettings failed, detail: "
                            + queryRequest.toString()
                            + ", error: "
                            + e.getMessage());
        }
    }
}
