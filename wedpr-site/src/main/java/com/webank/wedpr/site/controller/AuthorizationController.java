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
import com.webank.wedpr.common.utils.PageRequest;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.authorization.model.*;
import com.webank.wedpr.components.authorization.service.AuthorizationService;
import com.webank.wedpr.components.token.auth.TokenUtils;
import com.webank.wedpr.components.token.auth.model.UserToken;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.ibatis.reflection.ArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        path = Constant.WEDPR_API_PREFIX + "/auth",
        produces = {"application/json"})
public class AuthorizationController {
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationController.class);

    @Autowired private AuthorizationService authorizationService;

    // create the authorization request
    @PostMapping("/createAuth")
    public WeDPRResponse createAuth(
            @RequestBody AuthRequest authRequest, HttpServletRequest request) {
        try {
            return authorizationService.createAuth(
                    TokenUtils.getLoginUser(request).getUsername(), authRequest);
        } catch (Exception e) {
            logger.warn("createAuth error, error: ", e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "createAuth error for " + e.getMessage());
        }
    }

    // update the authorization information(including to the auth-chain)
    @PostMapping("/updateAuth")
    public WeDPRResponse updateAuth(
            @RequestBody AuthRequest authRequest, HttpServletRequest request) {
        try {
            authRequest.setResetFollowers(Boolean.TRUE);
            return authorizationService.updateAuth(
                    TokenUtils.getLoginUser(request).getUsername(), authRequest, true);
        } catch (Exception e) {
            logger.warn("updateAuth error, error: ", e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "updateAuth error for " + e.getMessage());
        }
    }

    /**
     * query the todoList
     *
     * @param condition the condition
     * @param request the request to obtain the user information
     * @return the queried result
     */
    @PostMapping("/queryTODOList")
    public WeDPRResponse queryTODOList(
            @RequestBody SingleAuthRequest condition, HttpServletRequest request) {
        try {
            UserToken userToken = TokenUtils.getLoginUser(request);
            AuthListResponse authList =
                    this.authorizationService.queryTODOList(userToken.getUsername(), condition);
            WeDPRResponse response =
                    new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
            response.setData(authList);
            return response;
        } catch (Exception e) {
            logger.warn("queryTODOList error, condition: {}, error: ", condition.toString(), e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "queryTODOList failed for " + e.getMessage());
        }
    }

    // query the authorization-meta-information according to given condition
    @PostMapping("/queryAuthList")
    public WeDPRResponse queryAuthList(
            @RequestBody SingleAuthRequest condition, HttpServletRequest request) {
        try {
            return authorizationService.queryAuthList(
                    TokenUtils.getLoginUser(request).getUsername(), condition);
        } catch (Exception e) {
            logger.warn("queryAuthList error, condition: {}, reason: ", condition.toString(), e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "queryAuthList error for " + e.getMessage());
        }
    }
    // query the follower auth-list
    @PostMapping("/queryFollowerAuthList")
    public WeDPRResponse queryFollowerAuthList(
            @RequestBody AuthFollowerRequest authRequest, HttpServletRequest request) {
        try {
            String user = TokenUtils.getLoginUser(request).getUsername();
            authRequest.getAuthFollowerDO().setUserName(user);
            authRequest.getAuthFollowerDO().setAgency(WeDPRCommonConfig.getAgency());
            return authorizationService.queryFollowerAuthList(user, authRequest);
        } catch (Exception e) {
            logger.warn(
                    "queryFollowerAuthList error, condition: {}, reason: ",
                    authRequest.getAuthFollowerDO().toString(),
                    e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "queryFollowerAuthList error for " + e.getMessage());
        }
    }

    // query the auth-detail according to applicant and authID
    @GetMapping("/queryAuthDetail")
    public WeDPRResponse queryAuthDetail(@RequestParam String authID, HttpServletRequest request) {
        try {
            return authorizationService.queryAuthDetail(
                    TokenUtils.getLoginUser(request).getUsername(), authID);
        } catch (Exception e) {
            logger.warn("queryAuthDetail error, authID: {}, reason: ", authID, e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "queryAuthDetail error for " + e.getMessage());
        }
    }

    // close the authList
    @PostMapping("/closeAuthList")
    public WeDPRResponse closeAuthList(
            @RequestBody List<String> authList, HttpServletRequest request) {
        try {
            return authorizationService.closeAuthList(
                    TokenUtils.getLoginUser(request).getUsername(), authList);
        } catch (Exception e) {
            logger.warn(
                    "closeAuthList error, authIDList: {}, reason: ",
                    ArrayUtil.toString(authList),
                    e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "closeAuthList error for " + e.getMessage());
        }
    }

    // update the auth-result
    @PostMapping("/updateAuthResult")
    public WeDPRResponse updateAuthResult(
            @RequestBody AuthResultRequest authResultRequest, HttpServletRequest request) {
        try {
            return authorizationService.updateAuthResult(
                    TokenUtils.getLoginUser(request).getUsername(), authResultRequest);
        } catch (Exception e) {
            logger.warn(
                    "updateAuthResult error, authResult: {}, reason: ",
                    authResultRequest.toString(),
                    e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "updateAuthResult error for " + e.getMessage());
        }
    }

    /// the auth-template related interface
    // create auth-template
    @PostMapping("/template/createAuthTemplates")
    public WeDPRResponse createAuthTemplates(
            @RequestBody AuthTemplateRequest authTemplateRequest, HttpServletRequest request) {
        try {
            return authorizationService.createAuthTemplates(
                    TokenUtils.getLoginUser(request).getUsername(), authTemplateRequest);
        } catch (Exception e) {
            logger.warn(
                    "createAuthTemplates error, authTemplate: {}, reason: ",
                    authTemplateRequest.toString(),
                    e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "createAuthTemplates error for " + e.getMessage());
        }
    }

    // update auth-template
    @PostMapping("/template/updateAuthTemplates")
    public WeDPRResponse updateAuthTemplates(
            @RequestBody AuthTemplateRequest authTemplateRequest, HttpServletRequest request) {
        try {
            return authorizationService.updateAuthTemplates(
                    TokenUtils.getLoginUser(request).getUsername(), authTemplateRequest);
        } catch (Exception e) {
            logger.warn(
                    "updateAuthTemplates error, authTemplate: {}, reason: ",
                    authTemplateRequest.toString(),
                    e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "updateAuthTemplates error for " + e.getMessage());
        }
    }

    // delete the auth-template
    @DeleteMapping("/template/deleteAuthTemplates")
    public WeDPRResponse deleteAuthTemplates(
            @RequestBody AuthTemplatesDeleteRequest condition, HttpServletRequest servletRequest) {
        try {
            condition.setCreateUser(TokenUtils.getLoginUser(servletRequest).getUsername());
            return authorizationService.deleteAuthTemplates(condition);
        } catch (Exception e) {
            logger.warn(
                    "deleteAuthTemplates error, request: {}, reason: ", condition.toString(), e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "deleteAuthTemplates error for " + e.getMessage());
        }
    }

    // query auth-template-list according to user
    @PostMapping("/template/queryAuthTemplateList")
    public WeDPRResponse queryAuthTemplateList(
            @RequestBody PageRequest pageRequest, HttpServletRequest servletRequest) {
        try {
            return authorizationService.queryAuthTemplateList(null, pageRequest);
        } catch (Exception e) {
            logger.warn("queryAuthTemplateList error, reason: ", e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "queryAuthTemplateList error for " + e.getMessage());
        }
    }

    // query the auth-template-details according to the templateIDs
    @PostMapping("/template/queryAuthTemplateDetails")
    public WeDPRResponse queryAuthTemplateDetails(
            @RequestBody List<String> templateIDList, HttpServletRequest servletRequest) {
        try {
            return authorizationService.queryAuthTemplateDetails(
                    TokenUtils.getLoginUser(servletRequest).getUsername(), templateIDList);
        } catch (Exception e) {
            logger.warn(
                    "queryAuthTemplateDetails error, templateIDList: {}, reason: ",
                    ArrayUtil.toString(templateIDList),
                    e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "queryAuthTemplateDetails error for " + e.getMessage());
        }
    }
}
