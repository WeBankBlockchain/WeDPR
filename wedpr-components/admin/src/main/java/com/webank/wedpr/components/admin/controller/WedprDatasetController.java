package com.webank.wedpr.components.admin.controller;

import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.admin.common.Utils;
import com.webank.wedpr.components.admin.request.GetWedprDatasetListRequest;
import com.webank.wedpr.components.admin.service.WedprDatasetService;
import com.webank.wedpr.components.dataset.message.ListDatasetResponse;
import com.webank.wedpr.components.db.mapper.dataset.dao.Dataset;
import com.webank.wedpr.components.token.auth.model.UserToken;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据集记录表 前端控制器
 *
 * @author caryliao
 * @since 2024-08-29
 */
@RestController
@RequestMapping(
        path = Constant.WEDPR_API_PREFIX + "/admin",
        produces = {"application/json"})
@Slf4j
public class WedprDatasetController {
    @Autowired private WedprDatasetService wedprDatasetService;

    @GetMapping("/listDataset")
    public WeDPRResponse listDataset(
            @Valid GetWedprDatasetListRequest getWedprDatasetListRequest,
            HttpServletRequest request) {
        try {
            // check user permission
            UserToken userToken = Utils.checkPermission(request);
            ListDatasetResponse listDatasetResponse =
                    wedprDatasetService.listDataset(getWedprDatasetListRequest);
            return new WeDPRResponse(
                    Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG, listDatasetResponse);
        } catch (Exception e) {
            log.error("getDatasetList error", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, e.getMessage());
        }
    }

    @GetMapping("/queryDataset")
    public WeDPRResponse queryDataset(
            @RequestParam("datasetId") String datasetId, HttpServletRequest request) {
        try {
            // check user permission
            UserToken userToken = Utils.checkPermission(request);
            Dataset dataset = wedprDatasetService.getById(datasetId);
            return new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG, dataset);
        } catch (Exception e) {
            log.error("queryDataset error", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, e.getMessage());
        }
    }
}
