package com.webank.wedpr.components.admin.controller;

import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.admin.common.Utils;
import com.webank.wedpr.components.admin.request.GetWedprCertListRequest;
import com.webank.wedpr.components.admin.request.SetAgencyCertRequest;
import com.webank.wedpr.components.admin.response.*;
import com.webank.wedpr.components.admin.service.WedprCertService;
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
public class WedprCertController {
    @Autowired private WedprCertService wedprCertService;

    /**
     * 创建或更新机构证书
     *
     * @param request
     * @return
     */
    @PostMapping("/createCert")
    public WeDPRResponse createOrUpdateAgencyCert(HttpServletRequest request) {
        try {
            // check user permission
            UserToken userToken = Utils.checkPermission(request);
            String certId =
                    wedprCertService.createOrUpdateAgencyCert(request, userToken.getUsername());
            CreateOrUpdateWedprCertResponse response = new CreateOrUpdateWedprCertResponse();
            response.setCertId(certId);
            return new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG, response);
        } catch (Exception e) {
            log.error("create or update agency cert error", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, e.getMessage());
        }
    }

    /**
     * 删除机构证书
     *
     * @param certId
     * @param request
     * @return
     */
    @PostMapping("/deleteCert/{certId}")
    public WeDPRResponse deleteAgencyCert(
            @PathVariable("certId") String certId, HttpServletRequest request) {
        try {
            // check user permission
            UserToken userToken = Utils.checkPermission(request);
            wedprCertService.deleteAgencyCert(certId);
            DeleteWedprCertResponse response = new DeleteWedprCertResponse();
            response.setCertId(certId);
            return new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG, response);
        } catch (Exception e) {
            log.error("delete agency cert error", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, e.getMessage());
        }
    }

    /**
     * 设置机构证书启用还是禁用，存储0表示启用，存储1表示禁用，过期和有效都是通过查询获取有效期判断返回到客户端
     *
     * @param setAgencyCertRequest
     * @param request
     * @return
     */
    @PostMapping("/setCert")
    public WeDPRResponse setAgencyCert(
            @Valid @RequestBody SetAgencyCertRequest setAgencyCertRequest,
            HttpServletRequest request) {
        try {
            // check user permission
            UserToken userToken = Utils.checkPermission(request);
            wedprCertService.setAgencyCert(setAgencyCertRequest, userToken.getUsername());
            SetWedprCertResponse response = new SetWedprCertResponse();
            response.setCertId(setAgencyCertRequest.getCertId());
            return new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG, response);
        } catch (Exception e) {
            log.error("set agency cert error", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, e.getMessage());
        }
    }

    /**
     * 查询这个机构证书列表
     *
     * @param getWedprCertListRequest
     * @param request
     * @return
     */
    @GetMapping("/getCertList")
    public WeDPRResponse getAgencyCertList(
            @Valid GetWedprCertListRequest getWedprCertListRequest, HttpServletRequest request) {
        try {
            // check user permission
            UserToken userToken = Utils.checkPermission(request);
            GetWedprCertListResponse response =
                    wedprCertService.getAgencyCertList(getWedprCertListRequest);
            return new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG, response);
        } catch (Exception e) {
            log.error("get agency cert list error", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, e.getMessage());
        }
    }

    /**
     * 查询证书请求文件详情
     *
     * @param certId
     * @param request
     * @return
     */
    @GetMapping("/getCsrDetail/{certId}")
    public WeDPRResponse getCsrDetail(
            @PathVariable("certId") String certId, HttpServletRequest request) {
        try {
            // check user permission
            UserToken userToken = Utils.checkPermission(request);
            GetWedprCertDetailResponse response = wedprCertService.getAgencyCsrDetail(certId);
            return new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG, response);
        } catch (Exception e) {
            log.error("get agency csr detail error", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, e.getMessage());
        }
    }

    /**
     * 下载证书文件，zip压缩包形式，，包括根证书和机构证书
     *
     * @param certId
     * @param request
     * @return
     */
    @GetMapping("/downloadCert/{certId}")
    public WeDPRResponse downloadCert(
            @PathVariable("certId") String certId, HttpServletRequest request) {
        try {
            // check user permission
            UserToken userToken = Utils.checkPermission(request);
            DownloadCertResponse response = wedprCertService.downloadCert(certId);
            return new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG, response);
        } catch (Exception e) {
            log.error("download agency cert error", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, e.getMessage());
        }
    }

    /**
     * 下载证书生成脚本
     *
     * @param request
     * @return
     */
    @GetMapping("/downloadCertTool")
    public WeDPRResponse downloadCertTool(HttpServletRequest request) {
        try {
            // check user permission
            UserToken userToken = Utils.checkPermission(request);
            DownloadCertToolResponse response = wedprCertService.downloadCertScript();
            return new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG, response);
        } catch (Exception e) {
            log.error("download agency cert tool error", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, e.getMessage());
        }
    }
}
