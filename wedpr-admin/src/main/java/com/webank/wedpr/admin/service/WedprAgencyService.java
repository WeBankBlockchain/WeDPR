package com.webank.wedpr.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.webank.wedpr.admin.entity.WedprAgency;
import com.webank.wedpr.admin.request.CreateOrUpdateWedprAgencyRequest;
import com.webank.wedpr.admin.request.GetWedprAgencyListRequest;
import com.webank.wedpr.admin.request.SetWedprAgencyRequest;
import com.webank.wedpr.admin.response.GetAgencyStatisticsResponse;
import com.webank.wedpr.admin.response.GetWedprAgencyDetailResponse;
import com.webank.wedpr.admin.response.GetWedprAgencyListResponse;
import com.webank.wedpr.admin.response.GetWedprNoCertAgencyListResponse;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.token.auth.model.UserToken;
import java.util.concurrent.ExecutionException;

/**
 * 服务类
 *
 * @author caryliao
 * @since 2024-08-22
 */
public interface WedprAgencyService extends IService<WedprAgency> {
    String createOrUpdateAgency(
            CreateOrUpdateWedprAgencyRequest createOrUpdateWedprAgencyRequest, UserToken userToken)
            throws WeDPRException;

    GetWedprAgencyListResponse getWedprAgencyList(
            GetWedprAgencyListRequest getWedprAgencyListRequest);

    GetWedprAgencyDetailResponse getWedprAgencyDetail(String agencyId) throws WeDPRException;

    void deleteWedprAgency(String agencyId) throws WeDPRException;

    void setWedprAgency(SetWedprAgencyRequest setWedprAgencyRequest) throws WeDPRException;

    GetAgencyStatisticsResponse getAgencyStatistics()
            throws ExecutionException, InterruptedException;

    GetWedprNoCertAgencyListResponse getNoCertAgencyList();
}
