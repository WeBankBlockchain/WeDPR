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

package com.webank.wedpr.components.authorization.service;

import com.webank.wedpr.common.utils.PageRequest;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.authorization.model.*;
import java.util.List;

public interface AuthorizationService {
    /// the auth-request related interface
    // create the authorization request
    public abstract WeDPRResponse createAuth(String applicant, AuthRequest authRequest);
    // update the authorization information(including to the auth-chain)
    public abstract WeDPRResponse updateAuth(
            String applicant, AuthRequest authRequest, boolean updateContent);
    // query the authorization-meta-information according to given condition
    public abstract WeDPRResponse queryAuthList(String applicant, SingleAuthRequest condition);
    // query the follower auth-list
    public abstract WeDPRResponse queryFollowerAuthList(String user, AuthFollowerRequest request);

    // query the auth-detail according to applicant and authID
    public abstract WeDPRResponse queryAuthDetail(String applicant, String authID);

    // close the authList
    public abstract WeDPRResponse closeAuthList(String applicant, List<String> authList);

    // update the auth-result
    public abstract WeDPRResponse updateAuthResult(
            String authorizer, AuthResultRequest authResultRequest);

    /// the auth-template related interface
    // create auth-template
    public abstract WeDPRResponse createAuthTemplates(
            String user, AuthTemplateRequest authTemplateRequest);
    // update auth-template
    public abstract WeDPRResponse updateAuthTemplates(
            String user, AuthTemplateRequest authTemplateRequest);
    // delete the auth-template
    public abstract WeDPRResponse deleteAuthTemplates(AuthTemplatesDeleteRequest request);
    // query auth-template-list according to user
    public abstract WeDPRResponse queryAuthTemplateList(String user, PageRequest pageRequest);
    // query the auth-template-details according to the templateIDs
    public abstract WeDPRResponse queryAuthTemplateDetails(
            String user, List<String> templateIDList);

    public abstract AuthListResponse queryTODOList(String user, SingleAuthRequest condition)
            throws Exception;
}
