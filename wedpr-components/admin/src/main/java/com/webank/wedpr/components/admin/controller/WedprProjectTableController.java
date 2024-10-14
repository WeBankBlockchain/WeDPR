package com.webank.wedpr.components.admin.controller;

import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.admin.common.Utils;
import com.webank.wedpr.components.admin.request.GetWedprProjectListRequest;
import com.webank.wedpr.components.admin.response.ListProjectResponse;
import com.webank.wedpr.components.admin.service.WedprProjectTableService;
import com.webank.wedpr.components.token.auth.model.UserToken;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前端控制器
 *
 * @author caryliao
 * @since 2024-09-04
 */
@RestController
@RequestMapping(
        path = Constant.WEDPR_API_PREFIX + "/admin",
        produces = {"application/json"})
@Slf4j
public class WedprProjectTableController {
    @Autowired private WedprProjectTableService wedprProjectTableService;

    @GetMapping("/listProject")
    public WeDPRResponse listProject(
            @Valid GetWedprProjectListRequest getWedprProjectListRequest,
            HttpServletRequest request) {
        try {
            // check user permission
            UserToken userToken = Utils.checkPermission(request);
            ListProjectResponse listProjectResponse =
                    wedprProjectTableService.listProject(getWedprProjectListRequest);
            return new WeDPRResponse(
                    Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG, listProjectResponse);
        } catch (Exception e) {
            log.error("listProject error", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, e.getMessage());
        }
    }
}
