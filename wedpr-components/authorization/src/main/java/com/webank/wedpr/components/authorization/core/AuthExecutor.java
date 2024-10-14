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

package com.webank.wedpr.components.authorization.core;

import com.webank.wedpr.common.config.WeDPRCommonConfig;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.ThreadPoolService;
import com.webank.wedpr.components.authorization.dao.AuthMapperWrapper;
import com.webank.wedpr.components.authorization.dao.AuthorizationDO;
import com.webank.wedpr.components.authorization.model.AuthRequest;
import com.webank.wedpr.components.authorization.service.impl.AuthAction;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthExecutor {
    public static final Logger logger = LoggerFactory.getLogger(AuthExecutor.class);

    public interface SyncResourceHandler {
        public void sync(String trigger, AuthAction action, String resourceContent);
    }

    private final ThreadPoolService threadPoolService;
    private final AuthNotifier authNotifier;
    private final AuthMapperWrapper authMapperWrapper;
    private Boolean isLeader = Boolean.FALSE;
    private final SyncResourceHandler syncResourceHandler;

    public AuthExecutor(
            ThreadPoolService threadPoolService,
            AuthNotifier authNotifier,
            AuthMapperWrapper authMapperWrapper,
            SyncResourceHandler syncResourceHandler) {
        this.threadPoolService = threadPoolService;
        this.authNotifier = authNotifier;
        this.authMapperWrapper = authMapperWrapper;
        this.syncResourceHandler = syncResourceHandler;
    }

    public void onLeaderSwitch(boolean isLeader, String leaderID) {
        if (!isLeader) {
            this.isLeader = false;
            return;
        }
        switchToLeader();
        this.isLeader = true;
    }

    // recover the ApproveSuccess tasks
    private void switchToLeader() {
        logger.info("switchToLeader, recover the ApproveSuccess task");
        AuthorizationDO condition = new AuthorizationDO(true);
        condition.setStatus(AuthorizationDO.AuthStatus.ApproveSuccess.getStatus());
        List<String> authList =
                this.authMapperWrapper
                        .getAuthMapper()
                        .queryAuthByCondition(
                                condition, this.authNotifier.getApplyTypeListWithHandler());
        if (authList == null || authList.isEmpty()) {
            return;
        }
        for (String auth : authList) {
            onApproveSuccess(auth);
        }
    }

    public void onApproveSuccess(String authID) {
        if (!isLeader) {
            return;
        }
        this.threadPoolService
                .getThreadPool()
                .execute(
                        new Runnable() {
                            @Override
                            public void run() {
                                if (!isLeader) {
                                    return;
                                }
                                execute(authID);
                            }
                        });
    }

    private void execute(String authID) {
        AuthorizationDO authDetail = null;
        try {
            authDetail =
                    this.authMapperWrapper.queryAuth(
                            authID, this.authNotifier.getApplyTypeListWithHandler());
            if (authDetail == null) {
                logger.info(
                        "onApproveSuccess, return directly for the auth no need to execute. auth: {}",
                        authDetail);
                return;
            }
            logger.info(
                    "onApproveSuccess, auth: {}, title: {}, type: {}",
                    authDetail.getId(),
                    authDetail.getApplyTitle(),
                    authDetail.getApplyType());
            logger.info(
                    "onApproveSuccess, execute the handler, auth: {}, type: {}",
                    authDetail.getId(),
                    authDetail.getApplyType());
            // Note: the handler should exist
            AuthNotifier.NotifyHandler handler =
                    this.authNotifier.getNotifyHandler(authDetail.getApplyType());
            handler.execute(new AuthNotifier.ExecuteArgs(authDetail));
            onExecuteSuccess(authDetail);
        } catch (Exception e) {
            logger.warn(
                    "onApproveSuccess, execute the handler exception, auth: {}, error: ",
                    authID,
                    e);
            String errorMsg = "execute auth " + authID + " failed for " + e.getMessage();
            // TODO: fix logic for authDetail is null
            if (authDetail != null) {
                onExecuteFailed(authDetail, errorMsg);
            }
        }
    }

    // update the status to ProgressSuccess
    private void onExecuteSuccess(AuthorizationDO authDetail) {
        updateAuthStatus(
                authDetail, AuthorizationDO.AuthStatus.ProgressSuccess, Constant.WEDPR_SUCCESS_MSG);
    }

    // update the status to ProgressFailed
    private void onExecuteFailed(AuthorizationDO authDetail, String errorMsg) {
        updateAuthStatus(authDetail, AuthorizationDO.AuthStatus.ProgressFailed, errorMsg);
    }

    private void updateAuthStatus(
            AuthorizationDO authDetail,
            AuthorizationDO.AuthStatus authStatus,
            String executeResult) {
        try {
            // only the applicant-agency update the status
            if (authDetail.getApplicantAgency().compareToIgnoreCase(WeDPRCommonConfig.getAgency())
                    != 0) {
                return;
            }
            logger.info(
                    "onApproveSuccess: updateAuthStatus, update the status to {}, auth: {}, type: {}",
                    authStatus.getStatus(),
                    authDetail.getId(),
                    authDetail.getApplyType());
            AuthorizationDO updatedAuth =
                    new AuthorizationDO(authDetail.getId(), authStatus.getStatus());
            updatedAuth.setExecuteResult(executeResult);
            // sync the status
            this.syncResourceHandler.sync(
                    authDetail.getApplicant(),
                    AuthAction.UpdateAuth,
                    new AuthRequest(updatedAuth).serialize());
        } catch (Exception e) {
            // TODO: the exception case
            logger.warn(
                    "onApproveSuccess, update status to {} failed, auth: {}, type: {}, error: ",
                    authStatus.getStatus(),
                    authDetail.getId(),
                    authDetail.getApplyType());
        }
    }
}
