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

package com.webank.wedpr.components.authorization.dao;

import com.github.pagehelper.PageInfo;
import com.webank.wedpr.common.config.WeDPRCommonConfig;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.PageRequest;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.authorization.model.*;
import com.webank.wedpr.components.meta.resource.follower.dao.FollowerDO;
import com.webank.wedpr.components.meta.resource.follower.dao.FollowerMapper;
import com.webank.wedpr.components.mybatis.PageHelperWrapper;
import java.util.ArrayList;
import java.util.List;
import org.apache.ibatis.reflection.ArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AuthMapperWrapper {
    private static final Logger logger = LoggerFactory.getLogger(AuthMapperWrapper.class);
    @Autowired private AuthMapper authMapper;
    @Autowired private FollowerMapper followerMapper;

    public AuthMapper getAuthMapper() {
        return authMapper;
    }

    public List<AuthorizationTemplateDO> queryAuthTemplateByName(String templateName) {
        AuthorizationTemplateDO condition = new AuthorizationTemplateDO(templateName);
        condition.setTemplateID("");
        return this.authMapper.queryAuthTemplateMetaList(condition);
    }

    @Transactional(rollbackFor = Exception.class)
    public void createAuth(AuthRequest authRequest) {
        if (authRequest.getAuthList() == null || authRequest.getAuthList().isEmpty()) {
            return;
        }
        this.authMapper.batchInsertAuthList(authRequest.getAuthList());
        for (AuthorizationDO authorizationDO : authRequest.getAuthList()) {
            if (authorizationDO.getFollowerDOList() == null
                    || authorizationDO.getFollowerDOList().isEmpty()) {
                continue;
            }
            this.followerMapper.batchInsert(authorizationDO.getFollowerDOList());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateAuth(AuthRequest authRequest) {
        if (authRequest.getAuthList() == null || authRequest.getAuthList().isEmpty()) {
            return;
        }
        this.authMapper.batchUpdateAuth(authRequest.getAuthList());
        for (AuthorizationDO authorizationDO : authRequest.getAuthList()) {
            if (authRequest.isResetFollowers()) {
                this.followerMapper.deleteFollower(authorizationDO.getId());
            }
            if (authorizationDO.getFollowerDOList() == null
                    || authorizationDO.getFollowerDOList().isEmpty()) {
                continue;
            }
            this.followerMapper.batchInsert(authorizationDO.getFollowerDOList());
        }
    }

    public AuthorizationDO queryAuth(String authID, List<String> applyTypeList) {
        AuthorizationDO detail = this.authMapper.queryAuthDetail(authID, applyTypeList);
        if (detail == null) {
            return null;
        }
        // query the followers
        List<FollowerDO> followerList =
                queryFollowerByType(detail.getId(), FollowerDO.FollowerType.AUTH_FOLLOWER);
        if (followerList == null || followerList.isEmpty()) {
            return detail;
        }
        detail.setFollowerDOList(followerList);
        return detail;
    }

    private List<FollowerDO> queryFollowerByType(
            String resourceID, FollowerDO.FollowerType followerType) {
        // query the followers
        FollowerDO condition = new FollowerDO(true);
        condition.setResourceID(resourceID);
        condition.setFollowerType(followerType.getType());
        return this.followerMapper.queryFollowerList(condition);
    }

    public WeDPRResponse queryAuthDetail(String user, String authID) {
        WeDPRResponse response =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        try {
            // query the authDetail without followers
            AuthorizationDO detail = this.queryAuth(authID, null);
            if (detail == null) {
                return response;
            }
            // check the permission: the applicant/currentApplyNode has the permission to access
            // auth-detail
            if (user.compareToIgnoreCase(detail.getApplicant()) == 0
                    || user.compareToIgnoreCase(detail.getCurrentApplyNode()) == 0) {
                response.setData(detail);
                return response;
            }
            // check the permission: the follower has the permission to access the auth-detail
            if (detail.getFollowers().contains(user)) {
                response.setData(detail);
                return response;
            }
            // check the permission: the authorizer has the permission to access the auth-detail
            List<FollowerDO> authorizerList =
                    queryFollowerByType(authID, FollowerDO.FollowerType.AUTH_AUDITOR);
            for (FollowerDO followerDO : authorizerList) {
                if (followerDO.getUserName().compareToIgnoreCase(user) == 0
                        && followerDO.getAgency().compareToIgnoreCase(WeDPRCommonConfig.getAgency())
                                == 0) {
                    response.setData(detail);
                    return response;
                }
            }
            response.setCode(Constant.WEDPR_FAILED);
            response.setMsg("user " + user + " has no permission to access auth: " + authID);
            logger.warn(
                    "queryAuthDetail failed for no permission, user: {}, auth: {}, applicant: {}, currentApplyNode:{}, followers: {}",
                    user,
                    authID,
                    detail.getApplicant(),
                    detail.getCurrentApplyNode(),
                    detail.getFollowers());
            return response;
        } catch (Exception e) {
            logger.warn("queryAuthDetail exception, user: {}, auth: {}, error: ", user, authID, e);
            response.setCode(Constant.WEDPR_FAILED);
            response.setMsg(
                    "queryAuthDetail failed for: "
                            + e.getMessage()
                            + ", user: "
                            + user
                            + ", authID: "
                            + authID);
        }
        return response;
    }

    public WeDPRResponse queryAuthTemplateList(String user, PageRequest pageRequest) {
        WeDPRResponse response =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        AuthorizationTemplateDO condition = new AuthorizationTemplateDO(true);
        condition.setCreateUser(user);
        try (PageHelperWrapper pageHelperWrapper = new PageHelperWrapper(pageRequest)) {
            List<AuthorizationTemplateDO> authList =
                    this.authMapper.queryAuthTemplateMetaList(condition);
            long count = new PageInfo<AuthorizationTemplateDO>(authList).getTotal();
            response.setData(new AuthTemplateListResponse(count, authList));
        } catch (Exception e) {
            logger.warn("queryAuthTemplateList failed, user: {}, error: ", user, e);
            response.setCode(Constant.WEDPR_FAILED);
            response.setMsg(
                    "queryAuthTemplateList for user " + user + " failed, error: " + e.getMessage());
        }
        return response;
    }

    public WeDPRResponse queryAuthTemplateDetails(String user, List<String> templateNameList) {
        WeDPRResponse response =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        try {
            List<AuthorizationTemplateDO> templateDOList =
                    this.authMapper.queryAuthTemplateDetails(templateNameList);
            if (templateDOList == null || templateNameList.isEmpty()) {
                return response;
            }
            List<AuthorizationTemplateDO> result = new ArrayList<>();
            for (AuthorizationTemplateDO templateDO : templateDOList) {
                if (templateDO.getCreateUser().compareToIgnoreCase(user) == 0
                        || templateDO
                                        .getCreateUser()
                                        .compareToIgnoreCase(Constant.DEFAULT_OPEN_SYMBOL)
                                == 0) {
                    result.add(templateDO);
                }
            }
            response.setData(result);
        } catch (Exception e) {
            logger.warn(
                    "queryAuthTemplateDetails exception, user: {}, templateIDList: {}",
                    user,
                    ArrayUtil.toString(templateNameList));
            response.setCode(Constant.WEDPR_FAILED);
            response.setMsg(
                    "queryAuthTemplateDetails for "
                            + user
                            + " failed, templateID: "
                            + ArrayUtil.toString(templateNameList)
                            + ", error: "
                            + e.getMessage());
        }
        return response;
    }

    public WeDPRResponse queryAuthList(String applicant, SingleAuthRequest condition) {
        WeDPRResponse response =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        condition.getAuthorizationDO().setApplicant(applicant);
        try (PageHelperWrapper pageHelperWrapper = new PageHelperWrapper(condition)) {
            List<AuthorizationDO> authList =
                    this.authMapper.queryAuthMetaList(
                            condition.getAuthorizationDO(),
                            condition.getAuthorizationDO().getAuthStatusList());
            response.setData(
                    new AuthListResponse(
                            new PageInfo<AuthorizationDO>(authList).getTotal(), authList));
        } catch (Exception e) {
            logger.warn(
                    "queryAuthList exception, applicant: {}, condition: {}, error: ",
                    applicant,
                    condition.toString(),
                    e);
            response.setCode(Constant.WEDPR_FAILED);
            response.setMsg(
                    "queryAuthList for "
                            + applicant
                            + " failed, condition: "
                            + condition.toString()
                            + ", error: "
                            + e.getMessage());
        }
        return response;
    }

    public WeDPRResponse queryFollowerAuthList(String user, AuthFollowerRequest request) {
        WeDPRResponse response =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        request.getAuthFollowerDO().setUserName(user);
        List<FollowerDO> followerDOList =
                this.followerMapper.queryFollowerList(request.getAuthFollowerDO());
        if (followerDOList == null || followerDOList.isEmpty()) {
            return response;
        }
        List<String> authIDList = new ArrayList<>();
        for (FollowerDO followerDO : followerDOList) {
            authIDList.add(followerDO.getResourceID());
        }
        try (PageHelperWrapper wrapper = new PageHelperWrapper(request)) {
            AuthorizationDO condition = request.getCondition();
            condition.setAuthIDList(authIDList);
            // query the related authMeta
            List<AuthorizationDO> authList =
                    this.authMapper.queryAuthMetaList(condition, condition.getAuthStatusList());
            response.setData(
                    new AuthListResponse(
                            new PageInfo<AuthorizationDO>(authList).getTotal(), authList));
        } catch (Exception e) {
            logger.warn(
                    "queryFollowerAuthList exception, condition: {}, error: ",
                    request.toString(),
                    e);
            response.setCode(Constant.WEDPR_FAILED);
            response.setMsg(
                    "queryFollowerAuthList failed for: "
                            + e.getMessage()
                            + ", condition: "
                            + request.toString());
        }
        return response;
    }
}
