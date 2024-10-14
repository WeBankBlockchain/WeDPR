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
import com.webank.wedpr.common.config.WeDPRCommonConfig;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.integration.jupyter.dao.JupyterInfoDO;
import com.webank.wedpr.components.integration.jupyter.service.JupyterService;
import com.webank.wedpr.components.mybatis.PageHelperWrapper;
import com.webank.wedpr.components.token.auth.TokenUtils;
import com.webank.wedpr.components.token.auth.model.UserToken;
import com.webank.wedpr.site.controller.request.JupyterRequest;
import com.webank.wedpr.site.controller.response.JupyterResponse;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        path = Constant.WEDPR_API_PREFIX + "/jupyter",
        produces = {"application/json"})
public class JupyterController {
    private static final Logger logger = LoggerFactory.getLogger(JupyterController.class);

    @Autowired private JupyterService jupyterService;

    /**
     * allocate the jupyter
     *
     * @param request the request to obtain user information
     * @return the allocated jupyterID
     */
    @GetMapping("/allocate")
    public WeDPRResponse allocate(HttpServletRequest request) {
        try {
            UserToken userToken = TokenUtils.getLoginUser(request);
            String jupyterID =
                    this.jupyterService.allocate(
                            userToken.getUsername(), WeDPRCommonConfig.getAgency());
            WeDPRResponse response =
                    new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
            response.setData(jupyterID);
            return response;
        } catch (Exception e) {
            logger.warn("allocate jupyter failed, error: ", e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "allocate jupyter failed for " + e.getMessage());
        }
    }

    /**
     * query jupyter by condition
     *
     * @param queryRequest the query request
     * @param request the request to obtain user information
     * @return
     */
    @PostMapping("/query")
    public WeDPRResponse queryJupyters(
            @RequestBody JupyterRequest queryRequest, HttpServletRequest request) {
        try (PageHelperWrapper pageHelperWrapper = new PageHelperWrapper(queryRequest)) {
            UserToken userToken = TokenUtils.getLoginUser(request);
            List<JupyterInfoDO> result =
                    this.jupyterService.queryJupyters(
                            userToken.isAdmin(),
                            userToken.getUsername(),
                            queryRequest.getCondition());
            WeDPRResponse response =
                    new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
            response.setData(
                    new JupyterResponse(new PageInfo<JupyterInfoDO>(result).getTotal(), result));
            return response;
        } catch (Exception e) {
            logger.warn(
                    "queryJupyters jupyter failed, condition: {},  error: ",
                    queryRequest.toString(),
                    e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED,
                    "queryJupyters failed for "
                            + e.getMessage()
                            + ", requestDetail: "
                            + queryRequest.toString());
        }
    }

    /**
     * open jupyter
     *
     * @param id the jupyter id
     * @param request the request to obtain user information
     * @return the opened jupyter information
     */
    @GetMapping("/open")
    public WeDPRResponse open(@RequestParam String id, HttpServletRequest request) {
        try {
            UserToken userToken = TokenUtils.getLoginUser(request);
            JupyterInfoDO jupyterInfo = this.jupyterService.open(userToken.getUsername(), id);
            WeDPRResponse response =
                    new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
            response.setData(jupyterInfo);
            return response;
        } catch (Exception e) {
            logger.warn("open jupyter failed, id: {},  error: ", id, e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED,
                    "open failed for " + id + " failed, reason:" + e.getMessage());
        }
    }

    /**
     * close the jupyter
     *
     * @param id the jupyter id
     * @param request the request to obtain user information
     * @return the closed jupyter information
     */
    @GetMapping("/close")
    public WeDPRResponse close(@RequestParam String id, HttpServletRequest request) {
        try {
            UserToken userToken = TokenUtils.getLoginUser(request);
            JupyterInfoDO jupyterInfo = this.jupyterService.close(userToken.getUsername(), id);
            WeDPRResponse response =
                    new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
            response.setData(jupyterInfo);
            return response;
        } catch (Exception e) {
            logger.warn("close jupyter failed, id: {},  error: ", id, e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED,
                    "close failed for " + id + " failed, reason:" + e.getMessage());
        }
    }

    /**
     * destroy the jupyter
     *
     * @param id the jupyter id
     * @param request the request to obtain user information
     * @return
     */
    @DeleteMapping("/destroy")
    public WeDPRResponse destroy(@RequestParam String id, HttpServletRequest request) {
        try {
            UserToken userToken = TokenUtils.getLoginUser(request);
            boolean success =
                    this.jupyterService.destroy(
                            userToken.isAdmin(),
                            userToken.getUsername(),
                            WeDPRCommonConfig.getAgency(),
                            id);
            WeDPRResponse response =
                    new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
            response.setData(success);
            return response;
        } catch (Exception e) {
            logger.warn("destroy jupyter failed, id: {},  error: ", id, e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED,
                    "destroy failed for " + id + " failed, reason:" + e.getMessage());
        }
    }
}
