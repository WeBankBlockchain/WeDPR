package com.webank.wedpr.components.admin.controller;

import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.admin.common.Utils;
import com.webank.wedpr.components.admin.request.GetDatasetDateLineRequest;
import com.webank.wedpr.components.admin.request.GetJobDateLineRequest;
import com.webank.wedpr.components.admin.response.*;
import com.webank.wedpr.components.admin.service.WedprAgencyService;
import com.webank.wedpr.components.admin.service.WedprDatasetService;
import com.webank.wedpr.components.admin.service.WedprJobTableService;
import com.webank.wedpr.components.token.auth.model.UserToken;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author caryliao
 * @since 2024-09-10
 */
@RestController
@RequestMapping(
        path = Constant.WEDPR_API_PREFIX + "/admin/dashboard",
        produces = {"application/json"})
@Slf4j
public class WedprDashboardController {
    @Autowired private WedprDatasetService wedprDatasetService;
    @Autowired private WedprJobTableService wedprJobTableService;
    @Autowired private WedprAgencyService wedprAgencyService;

    @GetMapping("/dataset")
    public WeDPRResponse getDatasetStatistics(HttpServletRequest request) {
        try {
            // check user permission
            UserToken userToken = Utils.checkPermission(request);
            GetDatasetStatisticsResponse response = wedprDatasetService.getDatasetStatistics();
            return new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG, response);
        } catch (Exception e) {
            log.error("getDatasetStatistics error", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, e.getMessage());
        }
    }

    @GetMapping("/dataset-dateline")
    public WeDPRResponse getDatasetDateLine(
            GetDatasetDateLineRequest getDatasetDateLineRequest, HttpServletRequest request) {
        try {
            // check user permission
            UserToken userToken = Utils.checkPermission(request);
            GetDatasetLineResponse response =
                    wedprDatasetService.getDatasetDateLine(getDatasetDateLineRequest);
            return new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG, response);
        } catch (Exception e) {
            log.error("getDatasetDateLine error", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, e.getMessage());
        }
    }

    @GetMapping("/job")
    public WeDPRResponse getJobStatistics(HttpServletRequest request) {
        try {
            // check user permission
            UserToken userToken = Utils.checkPermission(request);
            GetJobStatisticsResponse response = wedprJobTableService.getJobStatistics();
            return new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG, response);
        } catch (Exception e) {
            log.error("getJobStatistics error", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, e.getMessage());
        }
    }

    @GetMapping("/job-dateline")
    public WeDPRResponse getJobDateLine(
            GetJobDateLineRequest getJobDateLineRequest, HttpServletRequest request) {
        try {
            // check user permission
            UserToken userToken = Utils.checkPermission(request);
            GetJobLineResponse response =
                    wedprJobTableService.getJobDateLine(getJobDateLineRequest);
            return new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG, response);
        } catch (Exception e) {
            log.error("getJobDateLine error", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, e.getMessage());
        }
    }

    @GetMapping("/agency")
    public WeDPRResponse getAgencyStatistics(HttpServletRequest request) {
        try {
            // check user permission
            UserToken userToken = Utils.checkPermission(request);
            GetAgencyStatisticsResponse response = wedprAgencyService.getAgencyStatistics();
            return new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG, response);
        } catch (Exception e) {
            log.error("getAgencyStatistics error", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, e.getMessage());
        }
    }
}
