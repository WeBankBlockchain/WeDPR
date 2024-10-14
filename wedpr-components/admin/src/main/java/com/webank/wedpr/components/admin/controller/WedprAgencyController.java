package com.webank.wedpr.components.admin.controller;

import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.admin.common.Utils;
import com.webank.wedpr.components.admin.request.CreateOrUpdateWedprAgencyRequest;
import com.webank.wedpr.components.admin.request.GetWedprAgencyListRequest;
import com.webank.wedpr.components.admin.request.SetWedprAgencyRequest;
import com.webank.wedpr.components.admin.response.*;
import com.webank.wedpr.components.admin.service.WedprAgencyService;
import com.webank.wedpr.components.token.auth.model.UserToken;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 前端控制器
 *
 * @author caryliao
 * @since 2024-08-22
 */
@RestController
@RequestMapping(
        path = Constant.WEDPR_API_PREFIX + "/admin",
        produces = {"application/json"})
@Slf4j
public class WedprAgencyController {
    @Autowired private WedprAgencyService wedprAgencyService;
    /**
     * 创建或更新机构信息
     *
     * @param createOrUpdateWedprAgencyRequest
     * @param request
     * @return
     */
    @PostMapping("/createAgency")
    public WeDPRResponse createOrUpdateAgency(
            @Valid @RequestBody CreateOrUpdateWedprAgencyRequest createOrUpdateWedprAgencyRequest,
            HttpServletRequest request) {
        try {
            // check user permission
            UserToken userToken = Utils.checkPermission(request);
            String agencyId =
                    wedprAgencyService.createOrUpdateAgency(
                            createOrUpdateWedprAgencyRequest, userToken);
            CreateOrUpdateWedprAgencyResponse response = new CreateOrUpdateWedprAgencyResponse();
            response.setAgencyId(agencyId);
            return new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG, response);
        } catch (Exception e) {
            log.error("create or update agency error", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, e.getMessage());
        }
    }

    /**
     * 查询机构列表
     *
     * @param getWedprAgencyListRequest
     * @param request
     * @return
     */
    @GetMapping("/getAgencyList")
    public WeDPRResponse getAgencyList(
            @Valid GetWedprAgencyListRequest getWedprAgencyListRequest,
            HttpServletRequest request) {
        try {
            // check user permission
            Utils.checkPermission(request);
            GetWedprAgencyListResponse response =
                    wedprAgencyService.getWedprAgencyList(getWedprAgencyListRequest);
            return new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG, response);
        } catch (Exception e) {
            log.error("get agency list error", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, e.getMessage());
        }
    }

    /**
     * 查询没有证书的机构列表
     *
     * @param request
     * @return
     */
    @GetMapping("/getNoCertAgencyList")
    public WeDPRResponse getNoCertAgencyList(HttpServletRequest request) {
        try {
            // check user permission
            Utils.checkPermission(request);
            GetWedprNoCertAgencyListResponse response = wedprAgencyService.getNoCertAgencyList();
            return new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG, response);
        } catch (Exception e) {
            log.error("get agency without cert error", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, e.getMessage());
        }
    }

    /**
     * 查询机构详情
     *
     * @param agencyId
     * @param request
     * @return
     */
    @GetMapping("/getAgencyDetail/{agencyId}")
    public WeDPRResponse getAgencyDetail(
            @PathVariable("agencyId") String agencyId, HttpServletRequest request) {
        try {
            // check user permission
            Utils.checkPermission(request);
            GetWedprAgencyDetailResponse response =
                    wedprAgencyService.getWedprAgencyDetail(agencyId);
            return new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG, response);
        } catch (Exception e) {
            log.error("get agency detail error", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, e.getMessage());
        }
    }

    /**
     * 删除机构
     *
     * @param agencyId
     * @param request
     * @return
     */
    @PostMapping("/deleteAgency/{agencyId}")
    public WeDPRResponse deleteAgency(
            @PathVariable("agencyId") String agencyId, HttpServletRequest request) {
        try {
            // check user permission
            Utils.checkPermission(request);
            wedprAgencyService.deleteWedprAgency(agencyId);
            DeleteWedprAgencyResponse response = new DeleteWedprAgencyResponse();
            response.setAgencyId(agencyId);
            return new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG, response);
        } catch (Exception e) {
            log.error("delete agency error", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, e.getMessage());
        }
    }

    /**
     * 启用/禁用机构
     *
     * @param setWedprAgencyRequest
     * @param request
     * @return
     */
    @PostMapping("/setAgency")
    public WeDPRResponse setAgency(
            @Valid @RequestBody SetWedprAgencyRequest setWedprAgencyRequest,
            HttpServletRequest request) {
        try {
            // check user permission
            Utils.checkPermission(request);
            wedprAgencyService.setWedprAgency(setWedprAgencyRequest);
            SetWedprAgencyResponse response = new SetWedprAgencyResponse();
            response.setAgencyId(setWedprAgencyRequest.getAgencyId());
            return new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG, response);
        } catch (Exception e) {
            log.error("delete agency error", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, e.getMessage());
        }
    }
}
