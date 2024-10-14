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

import com.webank.wedpr.common.config.WeDPRCommonConfig;
import com.webank.wedpr.common.utils.Common;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.authorization.dao.AuthMapperWrapper;
import com.webank.wedpr.components.authorization.dao.AuthorizationDO;
import com.webank.wedpr.components.authorization.dao.AuthorizationTemplateDO;
import com.webank.wedpr.components.authorization.model.*;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

public class ParamChecker {

    private final AuthMapperWrapper authMapperWrapper;
    private final String agency;

    public ParamChecker(String agency, AuthMapperWrapper authMapperWrapper) {
        this.authMapperWrapper = authMapperWrapper;
        this.agency = agency;
    }

    public void checkCreateAuthRequest(String applicant, AuthRequest authRequest) {
        for (AuthorizationDO authorizationDO : authRequest.getAuthList()) {
            checkCreateAuth(applicant, authorizationDO);
        }
    }

    public void checkUpdateAuthRequest(
            String applicant, AuthRequest authRequest, boolean updateContent) {
        for (AuthorizationDO authorizationDO : authRequest.getAuthList()) {
            checkUpdateAuth(applicant, authorizationDO, updateContent);
        }
    }

    private void checkCreateAuth(String applicant, AuthorizationDO authorizationDO) {
        // set the applicant
        authorizationDO.setApplicant(applicant);
        authorizationDO.setApplicantAgency(this.agency);
        // set the current node
        authorizationDO.setCurrentApplyNode(applicant);
        authorizationDO.setCurrentApplyNodeAgency(this.agency);
        authorizationDO.setStatus(AuthorizationDO.AuthStatus.ToConfirm.getStatus());
        authorizationDO
                .getAuthResult()
                .updateResult(
                        authorizationDO.getAuthChain(),
                        applicant,
                        new AuthResult.AuthResultDetail(AuthResultStatus.Submit));

        Common.requireNonEmpty("applicantAgency", authorizationDO.getApplicantAgency());
        Common.requireNonEmpty("applyTitle", authorizationDO.getApplyTitle());
        Common.requireNonEmpty("applyDesc", authorizationDO.getApplyDesc());

        Common.requireNonEmpty("applyChain", authorizationDO.getApplyChain());
        Common.requireNonEmpty("applyContent", authorizationDO.getApplyContent());
        Common.requireNonEmpty("applyTemplateName", authorizationDO.getApplyTemplateName());
        // check the existence of the applyTemplateName
        checkAuthTemplate(authorizationDO);
        // TODO: check the applyContent according to the authTemplate
        // TODO: check the applyChain
        checkApplyChain(applicant, authorizationDO);
    }

    @SneakyThrows(Exception.class)
    public void checkApplyChain(String applicant, AuthorizationDO authorizationDO) {
        if (authorizationDO.getAuthChain() == null
                || authorizationDO.getAuthChain().getChain().size() < 2) {
            throw new WeDPRException(
                    "Invalid authChain, must exist at least two authNode, and the first authNode should be the applicant-self!");
        }
        authorizationDO.getAuthChain().check();
        AuthChain.AuthNode firstAuthNode = authorizationDO.getAuthChain().getChain().get(0);
        if (firstAuthNode.getName().compareToIgnoreCase(applicant) != 0
                || firstAuthNode.getAgency().compareToIgnoreCase(WeDPRCommonConfig.getAgency())
                        != 0) {
            throw new WeDPRException(
                    "Invalid authChain, the first authNode should be the applicant-self");
        }
    }

    @SneakyThrows
    public void checkUpdateAuth(
            String applicant, AuthorizationDO authorizationDO, boolean updateContent) {
        Common.requireNonEmpty("id", authorizationDO.getId());
        if (updateContent) {
            Common.requireEmpty(
                    "status", authorizationDO.getStatus(), "no permission to update the status");
            Common.requireEmpty(
                    "result", authorizationDO.getResult(), "no permission to update the result ");
            Common.requireEmpty(
                    "currentApplyNode",
                    authorizationDO.getCurrentApplyNode(),
                    " no permission to update the currentApplyNode");
            Common.requireEmpty(
                    "getCurrentApplyNodeAgency",
                    authorizationDO.getCurrentApplyNodeAgency(),
                    " no permission to update the getCurrentApplyNodeAgency");
        }
        // check the updated template
        if (!StringUtils.isBlank(authorizationDO.getApplyTemplateName())) {
            checkAuthTemplate(authorizationDO);
        }
        // check authChain
        if (authorizationDO.getAuthChain() != null && updateContent) {
            checkApplyChain(applicant, authorizationDO);
        }
        // TODO: check the applyContent according to the authTemplate

        // check the auth-status
        AuthorizationDO condition = new AuthorizationDO(authorizationDO.getId());
        List<AuthorizationDO> result =
                this.authMapperWrapper.getAuthMapper().queryAuthMetaList(condition, null);
        if (result == null || result.isEmpty()) {
            throw new WeDPRException(
                    "Invalid updateAuth request for the auth not exist, auth: "
                            + authorizationDO.getId());
        }
        // can't update the readOnly auth
        if (result.get(0).getAuthStatus().readOnly()) {
            throw new WeDPRException(
                    "Invalid updateAuth request for the auth is read-only, auth: "
                            + authorizationDO.getId());
        }
        // update the authStatus to ToConfirm when original status is  AuthReject
        if (result.get(0).getAuthStatus().rejected()) {
            authorizationDO.setAuthStatus(AuthorizationDO.AuthStatus.ToConfirm);
        }
        // not first AuthNode, can't update the content
        if (result.get(0).getCurrentApplyNode().compareToIgnoreCase(applicant) != 0
                && updateContent) {
            throw new WeDPRException(
                    "Invalid updateAuth request, not permit to update the auth content now!");
        }
    }

    @SneakyThrows(WeDPRException.class)
    public AuthorizationDO checkAuthResultRequest(
            String authorizer, AuthResultRequest authResultRequest) {
        Common.requireNonEmpty("id", authResultRequest.getAuthID());
        authResultRequest.getAuthResultDetail().check();

        AuthorizationDO condition = new AuthorizationDO(authResultRequest.getAuthID());
        List<AuthorizationDO> result =
                this.authMapperWrapper.getAuthMapper().queryAuthMetaList(condition, null);
        if (result == null || result.isEmpty()) {
            throw new WeDPRException(
                    "Invalid update-auth-result request for the auth-id "
                            + condition.getId()
                            + " not exists!");
        }
        // check the node
        if (result.get(0).getCurrentApplyNode().compareToIgnoreCase(authorizer) != 0) {
            throw new WeDPRException(
                    "Invalid update-auth-result request for the authorizer "
                            + authorizer
                            + " not the current-node-authorizer!");
        }
        if (!result.get(0).getAuthStatus().approving()) {
            throw new WeDPRException(
                    "Invalid update-auth-result request for the auth not in Approving status, authID: "
                            + authResultRequest.getAuthID()
                            + ", authorizer: "
                            + authorizer);
        }
        return result.get(0);
    }

    @SneakyThrows(WeDPRException.class)
    private void checkAuthTemplate(AuthorizationDO authorizationDO) {
        List<AuthorizationTemplateDO> templateDOList =
                authMapperWrapper.queryAuthTemplateByName(authorizationDO.getApplyTemplateName());
        if (templateDOList == null) {
            throw new WeDPRException(
                    "Invalid createAuth request for the auth-template"
                            + authorizationDO.getApplyTemplateName()
                            + " is not exists");
        }
    }
}
