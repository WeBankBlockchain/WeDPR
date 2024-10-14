package com.webank.wedpr.components.admin.controller;

import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.admin.common.Utils;
import com.webank.wedpr.components.admin.request.GetJobByDatasetRequest;
import com.webank.wedpr.components.admin.response.ListJobResponse;
import com.webank.wedpr.components.admin.service.WedprJobDatasetRelationService;
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
 * @since 2024-09-06
 */
@RestController
@RequestMapping(
        path = Constant.WEDPR_API_PREFIX + "/admin",
        produces = {"application/json"})
@Slf4j
public class WedprJobDatasetRelationController {
    @Autowired private WedprJobDatasetRelationService wedprJobDatasetRelationService;

    @GetMapping("/queryJobsByDatasetId")
    public WeDPRResponse queryJobsByDatasetId(
            @Valid GetJobByDatasetRequest getJobByDatasetRequest, HttpServletRequest request) {
        try {
            // check user permission
            UserToken userToken = Utils.checkPermission(request);
            ListJobResponse listJobResponse =
                    wedprJobDatasetRelationService.queryJobsByDatasetId(getJobByDatasetRequest);
            return new WeDPRResponse(
                    Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG, listJobResponse);
        } catch (Exception e) {
            log.error("getJobByDatasetId error", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, e.getMessage());
        }
    }
}
