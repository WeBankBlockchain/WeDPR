package com.webank.wedpr.components.admin.controller;

import com.webank.wedpr.components.admin.common.Utils;
import com.webank.wedpr.components.admin.response.CreateOrUpdateWedprCertResponse;
import com.webank.wedpr.components.admin.service.WedprCertService;
import com.webank.wedpr.components.token.auth.model.UserToken;
import com.webank.wedpr.core.utils.Constant;
import com.webank.wedpr.core.utils.WeDPRResponse;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            String certId = wedprCertService.createOrUpdateCert(request, userToken.getUsername());
            CreateOrUpdateWedprCertResponse response = new CreateOrUpdateWedprCertResponse();
            response.setCertId(certId);
            return new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG, response);
        } catch (Exception e) {
            log.error("create or update cert error", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, e.getMessage());
        }
    }
}
