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

package com.webank.wedpr.components.meta.setting.template.service.impl;

import com.github.pagehelper.PageInfo;
import com.webank.wedpr.components.meta.setting.template.dao.SettingTemplateDO;
import com.webank.wedpr.components.meta.setting.template.dao.SettingTemplateMapper;
import com.webank.wedpr.components.meta.setting.template.model.TemplateSettingList;
import com.webank.wedpr.components.meta.setting.template.model.TemplateSettingQueryRequest;
import com.webank.wedpr.components.meta.setting.template.model.TemplateSettingRequest;
import com.webank.wedpr.components.meta.setting.template.service.TemplateSettingService;
import com.webank.wedpr.components.mybatis.PageHelperWrapper;
import com.webank.wedpr.core.config.WeDPRCommonConfig;
import com.webank.wedpr.core.utils.Constant;
import com.webank.wedpr.core.utils.WeDPRResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TemplateSettingServiceImpl implements TemplateSettingService {
    private static final Logger logger = LoggerFactory.getLogger(TemplateSettingServiceImpl.class);
    @Autowired private SettingTemplateMapper settingTemplateMapper;

    @Override
    public WeDPRResponse batchInsertTemplateSettings(
            Boolean admin, String user, TemplateSettingRequest settings) {
        settings.checkCreate(admin);
        settings.setOwnerInfo(admin, user);

        int result = this.settingTemplateMapper.insertSettings(settings.getTemplateList());
        WeDPRResponse response =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        response.setData(result);
        return response;
    }

    @Override
    public WeDPRResponse batchUpdateTemplateSettings(
            Boolean admin, String user, TemplateSettingRequest settings) {
        settings.checkUpdate(user, admin);
        WeDPRResponse response =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        response.setData(this.settingTemplateMapper.updateSettings(settings.getTemplateList()));
        return response;
    }

    @Override
    public WeDPRResponse deleteTemplateSettings(
            Boolean admin, String user, List<String> templateIDList) {
        WeDPRResponse response =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        // the admin can delete any settings
        if (admin) {
            response.setData(this.settingTemplateMapper.deleteSettings(null, null, templateIDList));
        } else {
            response.setData(
                    this.settingTemplateMapper.deleteSettings(
                            user, WeDPRCommonConfig.getAgency(), templateIDList));
        }
        return response;
    }

    @Override
    public WeDPRResponse querySettings(
            Boolean admin, String user, TemplateSettingQueryRequest request) {
        // the admin can query any records
        WeDPRResponse response =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        PageHelperWrapper pageHelperWrapper = new PageHelperWrapper(request);
        request.setOwnerCondition(admin, user);
        List<SettingTemplateDO> settings =
                this.settingTemplateMapper.querySetting(
                        request.getOnlyMeta(), request.getCondition());
        response.setData(
                new TemplateSettingList(
                        settings, new PageInfo<SettingTemplateDO>(settings).getTotal()));
        return response;
    }
}
