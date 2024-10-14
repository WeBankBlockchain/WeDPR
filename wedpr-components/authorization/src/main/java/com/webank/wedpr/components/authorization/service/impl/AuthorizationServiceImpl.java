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

import com.github.pagehelper.PageInfo;
import com.webank.wedpr.common.config.WeDPRCommonConfig;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.PageRequest;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.authorization.dao.AuthMapperWrapper;
import com.webank.wedpr.components.authorization.dao.AuthorizationDO;
import com.webank.wedpr.components.authorization.model.*;
import com.webank.wedpr.components.authorization.service.AuthorizationService;
import com.webank.wedpr.components.meta.resource.follower.dao.FollowerDO;
import com.webank.wedpr.components.mybatis.PageHelperWrapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationServiceImpl implements AuthorizationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationServiceImpl.class);

    private ParamChecker paramChecker;
    @Autowired private AuthMapperWrapper authMapperWrapper;

    @Autowired private AuthSyncer authSyncer;

    @PostConstruct
    public void init() {
        this.paramChecker = new ParamChecker(WeDPRCommonConfig.getAgency(), this.authMapperWrapper);
    }

    /// the auth-request related interface
    // create the authorization request
    @Override
    public WeDPRResponse createAuth(String applicant, AuthRequest authRequest) {
        WeDPRResponse response =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        try {
            logger.info(
                    "createAuth, applicant: {}, request: {}", applicant, authRequest.toString());

            this.paramChecker.checkCreateAuthRequest(applicant, authRequest);
            // sync the auth-request
            String resourceID =
                    this.authSyncer.sync(applicant, AuthAction.CreateAuth, authRequest.serialize());
            response.setData(new AuthResponse(resourceID, authRequest.getAuthIDList()));
            logger.info(
                    "createAuth and sync, applicant: {}, resourceID: {}", applicant, resourceID);
        } catch (Exception e) {
            logger.warn(
                    "createAuth exception, applicant: {}, request: {}, error: ",
                    applicant,
                    authRequest.toString(),
                    e);
            response.setCode(Constant.WEDPR_FAILED);
            response.setMsg(
                    "createAuth for "
                            + applicant
                            + " failed, authRequest: "
                            + authRequest.toString()
                            + ", error: "
                            + e.getMessage());
        }
        return response;
    }

    // update the authorization information(including to the auth-chain)
    @Override
    public WeDPRResponse updateAuth(
            String applicant, AuthRequest authRequest, boolean updateContent) {
        WeDPRResponse response =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        try {
            logger.info(
                    "updateAuth, applicant: {}, request: {}", applicant, authRequest.toString());
            this.paramChecker.checkUpdateAuthRequest(applicant, authRequest, updateContent);
            // sync the auth-request
            String resourceID =
                    this.authSyncer.sync(applicant, AuthAction.UpdateAuth, authRequest.serialize());
            response.setData(resourceID);
            logger.info(
                    "updateAuth and sync, applicant: {}, resourceID: {}", applicant, resourceID);
        } catch (Exception e) {
            logger.warn(
                    "updateAuth exception, applicant: {}, request: {}, error: ",
                    applicant,
                    authRequest.toString(),
                    e);
            response.setCode(Constant.WEDPR_FAILED);
            response.setMsg(
                    "updateAuth for "
                            + applicant
                            + " failed, authRequest: "
                            + authRequest.toString()
                            + ", error: "
                            + e.getMessage());
        }
        return response;
    }

    // query the authorization-meta-information according to given condition
    @Override
    public WeDPRResponse queryAuthList(String applicant, SingleAuthRequest condition) {
        return this.authMapperWrapper.queryAuthList(applicant, condition);
    }

    // query the follower auth-list
    @Override
    public WeDPRResponse queryFollowerAuthList(String user, AuthFollowerRequest request) {
        return this.authMapperWrapper.queryFollowerAuthList(user, request);
    }

    // query the auth-detail according to applicant and authID
    @Override
    public WeDPRResponse queryAuthDetail(String applicant, String authID) {
        return this.authMapperWrapper.queryAuthDetail(applicant, authID);
    }

    // close the authList
    @Override
    public WeDPRResponse closeAuthList(String applicant, List<String> authList) {
        AuthRequest authRequest =
                new AuthRequest(authList, AuthorizationDO.AuthStatus.ApproveCanceled.getStatus());
        return updateAuth(applicant, authRequest, false);
    }

    // update the auth-result
    @Override
    public WeDPRResponse updateAuthResult(String authorizer, AuthResultRequest authResultRequest) {
        try {
            AuthorizationDO result =
                    this.paramChecker.checkAuthResultRequest(authorizer, authResultRequest);
            AuthorizationDO updatedAuth =
                    new AuthorizationDO(
                            result.getId(),
                            authorizer,
                            this.authSyncer.getAgency(),
                            result.getResult());
            updatedAuth.setAuthChain(result.getAuthChain());
            // update the authResult
            updatedAuth.updateResult(authorizer, authResultRequest.getAuthResultDetail());
            if (authResultRequest.getAuthResultDetail().getAuthResultStatus().agree()) {
                // progress to next applyNode if the auth-result is agreed
                updatedAuth.progressToNextAuthNode();
                // update the status to approving
                if (result.getAuthStatus().toConfirmed()) {
                    updatedAuth.setAuthStatus(AuthorizationDO.AuthStatus.Approving);
                }
            } else {
                // return to the applicant if the auth-result is rejected
                updatedAuth.progressToApplicant(result.getApplicant(), result.getApplicantAgency());
            }
            // set the CurrentApplyNode to the follower
            FollowerDO followerDO =
                    new FollowerDO(
                            updatedAuth.getCurrentApplyNode(),
                            updatedAuth.getCurrentApplyNodeAgency(),
                            updatedAuth.getId(),
                            FollowerDO.FollowerType.AUTH_AUDITOR.getType());
            updatedAuth.setFollowerDOList(new ArrayList<>(Collections.singletonList(followerDO)));
            return updateAuth(authorizer, new AuthRequest(updatedAuth, false), false);
        } catch (Exception e) {
            logger.warn(
                    "updateAuthResult failed, authorizer: {}, id: {}, result: {}, error: ",
                    authorizer,
                    authResultRequest.getAuthID(),
                    authResultRequest.getAuthResultDetail().toString(),
                    e);
            WeDPRResponse response =
                    new WeDPRResponse(
                            Constant.WEDPR_FAILED,
                            "updateAuthResult failed,  authorizer: "
                                    + authorizer
                                    + ", ID: "
                                    + authResultRequest.getAuthID()
                                    + ", reason: "
                                    + e.getMessage());
            return response;
        }
    }

    ////// the auth-template related interface
    // create auth-template
    @Override
    public WeDPRResponse createAuthTemplates(String user, AuthTemplateRequest authTemplateRequest) {
        WeDPRResponse response =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        try {
            authTemplateRequest.checkCreate(user);
            String resourceID =
                    this.authSyncer.sync(
                            user, AuthAction.CreateAuthTemplates, authTemplateRequest.serialize());
            response.setData(new AuthResponse(resourceID, authTemplateRequest.getTemplateIDList()));
        } catch (Exception e) {
            logger.warn("createAuthTemplates failed, user: {}, error: ", user, e);
            response.setCode(Constant.WEDPR_FAILED);
            response.setMsg(
                    "createAuthTemplates failed, user: " + user + ", error: " + e.getMessage());
        }
        return response;
    }

    // update auth-template
    @Override
    public WeDPRResponse updateAuthTemplates(String user, AuthTemplateRequest authTemplateRequest) {
        WeDPRResponse response =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        try {
            authTemplateRequest.checkUpdate(user);
            String resourceID =
                    this.authSyncer.sync(
                            user, AuthAction.UpdateAuthTemplates, authTemplateRequest.serialize());
            response.setData(new AuthResponse(resourceID));
        } catch (Exception e) {
            logger.warn(
                    "updateAuthTemplates exception, user: {}, request: {}, error: ",
                    user,
                    authTemplateRequest.toString(),
                    e);
            response.setCode(Constant.WEDPR_FAILED);
            response.setMsg(
                    "updateAuthTemplates failed, user: " + user + ", error: " + e.getMessage());
        }
        return response;
    }

    // delete the auth-template
    @Override
    public WeDPRResponse deleteAuthTemplates(AuthTemplatesDeleteRequest request) {
        WeDPRResponse response =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        try {
            String resourceID =
                    this.authSyncer.sync(
                            request.getCreateUser(),
                            AuthAction.DeleteAuthTemplates,
                            request.serialize());
            response.setData(new AuthResponse(resourceID));
        } catch (Exception e) {
            logger.warn("deleteAuthTemplates failed, request: {}, error: ", request.toString(), e);
            response.setCode(Constant.WEDPR_FAILED);
            response.setMsg(
                    "deleteAuthTemplates failed, request: "
                            + request.toString()
                            + ", error: "
                            + e.getMessage());
        }
        return response;
    }

    // query auth-template-list according to user
    @Override
    public WeDPRResponse queryAuthTemplateList(String user, PageRequest pageRequest) {
        return this.authMapperWrapper.queryAuthTemplateList(user, pageRequest);
    }

    // query the auth-template-details according to the templateIDs
    @Override
    public WeDPRResponse queryAuthTemplateDetails(String user, List<String> templateNameList) {
        return this.authMapperWrapper.queryAuthTemplateDetails(user, templateNameList);
    }

    @Override
    public AuthListResponse queryTODOList(String user, SingleAuthRequest condition)
            throws Exception {
        condition.getAuthorizationDO().setCurrentApplyNode(user);
        condition.getAuthorizationDO().setCurrentApplyNodeAgency(WeDPRCommonConfig.getAgency());
        List<String> authStatusList = new ArrayList<>();
        authStatusList.add(AuthorizationDO.AuthStatus.ToConfirm.getStatus());
        authStatusList.add(AuthorizationDO.AuthStatus.Approving.getStatus());
        try (PageHelperWrapper pageHelperWrapper = new PageHelperWrapper(condition)) {
            List<AuthorizationDO> result =
                    this.authMapperWrapper
                            .getAuthMapper()
                            .queryAuthMetaList(condition.getAuthorizationDO(), authStatusList);
            return new AuthListResponse(new PageInfo<AuthorizationDO>(result).getTotal(), result);
        } catch (Exception e) {
            logger.warn(
                    "queryTODOList failed, user: {}, condition: {}, error: ",
                    user,
                    condition.toString(),
                    e);
            throw e;
        }
    }
}
