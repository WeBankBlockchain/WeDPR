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

package com.webank.wedpr.components.authorization.service.impl;

import com.webank.wedpr.common.utils.ThreadPoolService;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.authorization.WeDPRAuthNotifier;
import com.webank.wedpr.components.authorization.core.AuthExecutor;
import com.webank.wedpr.components.authorization.dao.AuthMapperWrapper;
import com.webank.wedpr.components.authorization.dao.AuthorizationDO;
import com.webank.wedpr.components.authorization.model.AuthRequest;
import com.webank.wedpr.components.authorization.model.AuthTemplateRequest;
import com.webank.wedpr.components.authorization.model.AuthTemplatesDeleteRequest;
import com.webank.wedpr.components.leader.election.LeaderElection;
import com.webank.wedpr.components.sync.ResourceSyncer;
import com.webank.wedpr.components.sync.core.ResourceActionRecord;
import com.webank.wedpr.components.sync.core.ResourceActionRecorderBuilder;
import com.webank.wedpr.components.uuid.generator.WeDPRUuidGenerator;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthSyncer {
    private static final Logger logger = LoggerFactory.getLogger(AuthSyncer.class);
    private final String agency;
    private final String resourceType;
    private final ResourceSyncer resourceSyncer;
    private final AuthMapperWrapper authMapperWrapper;
    private final ResourceActionRecorderBuilder resourceBuilder;
    private final AuthExecutor authExecutor;

    public class AuthCommitHandler implements ResourceSyncer.CommitHandler {
        @Override
        public void call(ResourceSyncer.CommitArgs args) throws WeDPRException {
            onReceiveResourceRecord(args);
        }
    }

    public AuthSyncer(
            String agency,
            String resourceType,
            ThreadPoolService threadPoolService,
            ResourceSyncer resourceSyncer,
            AuthMapperWrapper authMapperWrapper) {
        this.agency = agency;
        this.resourceType = resourceType;
        this.resourceSyncer = resourceSyncer;
        this.authMapperWrapper = authMapperWrapper;
        this.resourceBuilder = new ResourceActionRecorderBuilder(this.agency, this.resourceType);
        this.resourceSyncer.registerCommitHandler(this.resourceType, new AuthCommitHandler());
        this.authExecutor =
                new AuthExecutor(
                        threadPoolService,
                        WeDPRAuthNotifier.getAuthNotifier(),
                        this.authMapperWrapper,
                        new AuthExecutor.SyncResourceHandler() {
                            @Override
                            public void sync(
                                    String trigger, AuthAction action, String resourceContent) {
                                AuthSyncer.this.sync(trigger, action, resourceContent);
                            }
                        });
        // register the onLeaderSwitch handler
        this.resourceSyncer
                .getLeaderElection()
                .registerOnCampaignHandler(
                        new LeaderElection.CampaignHandler() {
                            @Override
                            public void onCampaign(boolean success, String leaderID) {
                                AuthSyncer.this.authExecutor.onLeaderSwitch(success, leaderID);
                            }
                        });
    }

    public String getAgency() {
        return this.agency;
    }

    public String sync(
            String trigger, String resourceID, AuthAction action, String resourceContent) {
        ResourceActionRecord record =
                this.resourceBuilder.build(resourceID, action.getAction(), resourceContent);
        this.resourceSyncer.sync(trigger, record);
        logger.debug("Sync resource, ID: {}, content: {}", resourceID, record.toString());
        return record.getResourceID();
    }

    public String sync(String trigger, AuthAction action, String resourceContent) {
        return sync(trigger, WeDPRUuidGenerator.generateID(), action, resourceContent);
    }

    private void onReceiveResourceRecord(ResourceSyncer.CommitArgs commitArgs) {
        if (commitArgs == null
                || commitArgs.getResourceActionRecord() == null
                || StringUtils.isBlank(commitArgs.getResourceActionRecord().getResourceAction())) {
            logger.warn("onReceiveResourceRecord, receive invalid resourceRecord");
            return;
        }
        logger.debug(
                "AuthSyncer, receive record: {}", commitArgs.getResourceActionRecord().toString());

        String action = commitArgs.getResourceActionRecord().getResourceAction();
        if (action.compareToIgnoreCase(AuthAction.CreateAuth.getAction()) == 0) {
            onCreateAuth(commitArgs);
        } else if (action.compareToIgnoreCase(AuthAction.UpdateAuth.getAction()) == 0) {
            onUpdateAuth(commitArgs);
        } else if (action.compareToIgnoreCase(AuthAction.CreateAuthTemplates.getAction()) == 0) {
            onCreateAuthTemplates(commitArgs);
        } else if (action.compareToIgnoreCase(AuthAction.UpdateAuthTemplates.getAction()) == 0) {
            onUpdateAuthTemplates(commitArgs);
        } else if (action.compareToIgnoreCase(AuthAction.DeleteAuthTemplates.getAction()) == 0) {
            onDeleteAuthTemplates(commitArgs);
        } else {
            logger.warn(
                    "onReceiveResourceRecord, invalid action: {}, resourceID: {}",
                    action,
                    commitArgs.getResourceActionRecord().getResourceID());
        }
    }

    @SneakyThrows(WeDPRException.class)
    private void onCreateAuth(ResourceSyncer.CommitArgs commitArgs) {
        AuthRequest authRequest =
                AuthRequest.deserialize(commitArgs.getResourceActionRecord().getResourceContent());
        if (authRequest == null) {
            throw new WeDPRException(
                    "onCreateAuthAction: invalid record action: "
                            + commitArgs.getResourceActionRecord().toString());
        }
        this.authMapperWrapper.createAuth(authRequest);
        logger.info(
                "onCreateAuthAction, commit resource {} success",
                commitArgs.getResourceActionRecord().getResourceID());
    }

    @SneakyThrows(WeDPRException.class)
    private void onUpdateAuth(ResourceSyncer.CommitArgs commitArgs) {
        AuthRequest authRequest =
                AuthRequest.deserialize(commitArgs.getResourceActionRecord().getResourceContent());
        if (authRequest == null) {
            throw new WeDPRException(
                    "onUpdateAuthAction: invalid record action: "
                            + commitArgs.getResourceActionRecord().toString());
        }
        this.authMapperWrapper.updateAuth(authRequest);
        // try to execute the approve-success resource-update-handler
        for (AuthorizationDO auth : authRequest.getAuthList()) {
            if (auth.getAuthStatus() == null || !auth.getAuthStatus().approveSuccess()) {
                continue;
            }
            this.authExecutor.onApproveSuccess(auth.getId());
        }
        logger.info(
                "onUpdateAuthAction, commit resource {} success",
                commitArgs.getResourceActionRecord().getResourceID());
    }

    private void onCreateAuthTemplates(ResourceSyncer.CommitArgs commitArgs) {
        AuthTemplateRequest request =
                AuthTemplateRequest.deserialize(
                        commitArgs.getResourceActionRecord().getResourceContent());
        if (request == null) {
            return;
        }
        this.authMapperWrapper.getAuthMapper().insertAuthTemplates(request.getTemplateList());
        logger.info(
                "onCreateAuthTemplates success, resource: {}",
                commitArgs.getResourceActionRecord().getResourceID());
    }

    private void onUpdateAuthTemplates(ResourceSyncer.CommitArgs commitArgs) {
        AuthTemplateRequest request =
                AuthTemplateRequest.deserialize(
                        commitArgs.getResourceActionRecord().getResourceContent());
        if (request == null) {
            return;
        }
        this.authMapperWrapper.getAuthMapper().updateAuthTemplates(request.getTemplateList());
        logger.info(
                "onUpdateAuthTemplates success, resource: {}",
                commitArgs.getResourceActionRecord().getResourceID());
    }

    private void onDeleteAuthTemplates(ResourceSyncer.CommitArgs commitArgs) {
        AuthTemplatesDeleteRequest request =
                AuthTemplatesDeleteRequest.deserialize(
                        commitArgs.getResourceActionRecord().getResourceContent());
        if (request == null) {
            return;
        }
        this.authMapperWrapper
                .getAuthMapper()
                .deleteAuthTemplates(request.getCreateUser(), request.getTemplates());
        logger.info(
                "onUpdateAuthTemplates success, resource: {}",
                commitArgs.getResourceActionRecord().getResourceID());
    }

    public AuthMapperWrapper getAuthMapperWrapper() {
        return this.authMapperWrapper;
    }
}
