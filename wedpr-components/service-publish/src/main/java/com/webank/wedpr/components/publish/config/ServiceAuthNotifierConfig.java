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

package com.webank.wedpr.components.publish.config;

import com.webank.wedpr.components.authorization.WeDPRAuthNotifier;
import com.webank.wedpr.components.authorization.core.AuthApplyType;
import com.webank.wedpr.components.authorization.core.AuthNotifier;
import com.webank.wedpr.components.authorization.dao.AuthorizationDO;
import com.webank.wedpr.components.db.mapper.service.publish.dao.ServiceAuthInfo;
import com.webank.wedpr.components.db.mapper.service.publish.dao.ServiceAuthMapper;
import com.webank.wedpr.components.publish.entity.ServiceAuthContent;
import com.webank.wedpr.core.utils.WeDPRException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceAuthNotifierConfig {
    private static final Logger logger = LoggerFactory.getLogger(ServiceAuthNotifierConfig.class);
    @Autowired private ServiceAuthMapper serviceAuthMapper;

    @Bean
    public void registerServiceAuthNotifyHandler() {
        // Register Service Approval Callback
        WeDPRAuthNotifier.getAuthNotifier()
                .registerNotifyHandler(
                        AuthApplyType.SERVICE.getType(),
                        new AuthNotifier.NotifyHandler() {
                            @Override
                            public void execute(AuthNotifier.ExecuteArgs args)
                                    throws WeDPRException {
                                try {
                                    AuthorizationDO authorizationDO = args.getAuthData();
                                    logger.info(
                                            "ServiceAuth approve, authInfo: {}",
                                            authorizationDO.toString());
                                    List<ServiceAuthInfo> serviceAuthInfos =
                                            ServiceAuthContent.toServiceAuthInfo(authorizationDO);
                                    serviceAuthMapper.batchInsertServiceAuth(serviceAuthInfos);
                                } catch (Exception e) {
                                    logger.warn(
                                            "Execute serviceAuth approval handler failed, authInfo: {}, error: ",
                                            args.getAuthData().toString(),
                                            e);
                                }
                            }
                        });
    }
}
