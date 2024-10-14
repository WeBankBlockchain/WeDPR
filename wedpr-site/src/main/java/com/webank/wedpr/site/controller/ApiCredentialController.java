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

import com.github.pagehelper.PageInfo;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.api.credential.dao.ApiCredentialDO;
import com.webank.wedpr.components.api.credential.service.ApiCredentialService;
import com.webank.wedpr.components.mybatis.PageHelperWrapper;
import com.webank.wedpr.components.token.auth.TokenUtils;
import com.webank.wedpr.components.token.auth.model.UserToken;
import com.webank.wedpr.site.controller.request.CredentialRequest;
import com.webank.wedpr.site.controller.response.CredentialResponse;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        path = Constant.WEDPR_API_PREFIX + "/credential",
        produces = {"application/json"})
public class ApiCredentialController {
    private static final Logger logger = LoggerFactory.getLogger(ApiCredentialController.class);

    @Autowired private ApiCredentialService apiCredentialService;

    /**
     * apply for new credential
     *
     * @param credentialDO the basic information for the credential
     * @param request the request(for obtain user information)
     * @return the id
     */
    @PostMapping("/applyFor")
    public WeDPRResponse applyFor(
            @RequestBody ApiCredentialDO credentialDO, HttpServletRequest request) {
        try {
            UserToken userToken = TokenUtils.getLoginUser(request);
            String id =
                    apiCredentialService.applyForCredential(userToken.getUsername(), credentialDO);
            WeDPRResponse response =
                    new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
            response.setData(id);
            return response;
        } catch (Exception e) {
            logger.warn(
                    "applyFor api credential exception, credential: {}, error: ",
                    credentialDO.toString(),
                    e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "apply for credential failed for " + e.getMessage());
        }
    }

    /**
     * query credentials by condition
     *
     * @param credentialRequest the condition
     * @param request the request(for obtain user information)
     * @return the queried credentials
     */
    @PostMapping("/query")
    public WeDPRResponse queryCredentials(
            @RequestBody CredentialRequest credentialRequest, HttpServletRequest request) {
        try (PageHelperWrapper pageHelperWrapper = new PageHelperWrapper(credentialRequest)) {
            UserToken userToken = TokenUtils.getLoginUser(request);
            List<ApiCredentialDO> credentialList =
                    apiCredentialService.queryCredentials(
                            userToken.getUsername(), credentialRequest.getCondition());
            WeDPRResponse response =
                    new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
            response.setData(
                    new CredentialResponse(
                            credentialList,
                            new PageInfo<ApiCredentialDO>(credentialList).getTotal()));
            return response;
        } catch (Exception e) {
            logger.warn(
                    "queryCredentials exception, request: {}, error: ",
                    credentialRequest.toString(),
                    e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED,
                    "query credentials failed for "
                            + e.getMessage()
                            + ", request detail: "
                            + credentialRequest.toString());
        }
    }

    /**
     * delete credentials by condition
     *
     * @param condition the condition to delete the credential
     * @param request the request(for obtain user information)
     * @return success/failed
     */
    @PostMapping("/delete")
    public WeDPRResponse deleteCredential(
            @RequestBody ApiCredentialDO condition, HttpServletRequest request) {
        try {
            UserToken userToken = TokenUtils.getLoginUser(request);
            boolean success =
                    apiCredentialService.deleteCredential(userToken.getUsername(), condition);
            WeDPRResponse response =
                    new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
            response.setData(success);
            return response;
        } catch (Exception e) {
            logger.warn(
                    "deleteCredential exception, condition: {}, error: ", condition.toString(), e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED,
                    "deleteCredential failed for "
                            + e.getMessage()
                            + ", condition: "
                            + condition.toString());
        }
    }

    /**
     * update credentials by condition
     *
     * @param condition the condition to update the credential
     * @param request the request(for obtain user information)
     * @return success/failed
     */
    @PostMapping("/update")
    public WeDPRResponse updateCredentials(
            @RequestBody ApiCredentialDO condition, HttpServletRequest request) {
        try {
            UserToken userToken = TokenUtils.getLoginUser(request);
            boolean success =
                    apiCredentialService.updateCredential(userToken.getUsername(), condition);
            WeDPRResponse response =
                    new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
            response.setData(success);
            return response;
        } catch (Exception e) {
            logger.warn(
                    "updateCredentials exception, condition: {}, error: ", condition.toString(), e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED,
                    "updateCredentials failed for "
                            + e.getMessage()
                            + ", condition: "
                            + condition.toString());
        }
    }
}
