package com.webank.wedpr.components.admin.controller;

import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.admin.common.Utils;
import com.webank.wedpr.components.admin.request.GetWedprAuditLogListRequest;
import com.webank.wedpr.components.admin.service.WedprSyncService;
import com.webank.wedpr.components.sync.service.impl.ResourceStatusResult;
import com.webank.wedpr.components.token.auth.model.UserToken;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 日志审计信息 前端控制器
 *
 * @author caryliao
 * @since 2024-08-29
 */
@RestController
@RequestMapping(
        path = Constant.WEDPR_API_PREFIX + "/admin",
        produces = {"application/json"})
@Slf4j
public class WedprAuditLogController {
    @Autowired private WedprSyncService wedprSyncService;

    @GetMapping("/queryRecordSyncStatus")
    public WeDPRResponse queryRecordSyncStatus(
            @Valid GetWedprAuditLogListRequest getWedprAuditLogListRequest,
            HttpServletRequest request) {
        try {
            // check user permission
            UserToken userToken = Utils.checkPermission(request);
            ResourceStatusResult resourceStatusResult =
                    wedprSyncService.queryRecordSyncStatus(getWedprAuditLogListRequest);
            return new WeDPRResponse(
                    Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG, resourceStatusResult);
        } catch (Exception e) {
            log.error("queryRecordSyncStatus error", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, e.getMessage());
        }
    }
}
