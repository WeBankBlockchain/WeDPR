package com.webank.wedpr.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.webank.wedpr.admin.entity.WedprCert;
import com.webank.wedpr.admin.request.GetWedprCertListRequest;
import com.webank.wedpr.admin.request.SetAgencyCertRequest;
import com.webank.wedpr.admin.response.DownloadCertResponse;
import com.webank.wedpr.admin.response.DownloadCertToolResponse;
import com.webank.wedpr.admin.response.GetWedprCertDetailResponse;
import com.webank.wedpr.admin.response.GetWedprCertListResponse;
import com.webank.wedpr.common.utils.WeDPRException;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;

/**
 * 服务类
 *
 * @author caryliao
 * @since 2024-08-22
 */
public interface WedprCertService extends IService<WedprCert> {
    String createOrUpdateAgencyCert(HttpServletRequest request, String username)
            throws WeDPRException, IOException;

    void deleteAgencyCert(String certId) throws WeDPRException;

    void setAgencyCert(SetAgencyCertRequest setAgencyCertRequest, String username)
            throws WeDPRException;

    GetWedprCertListResponse getAgencyCertList(GetWedprCertListRequest getWedprCertListRequest);

    GetWedprCertDetailResponse getAgencyCsrDetail(String certId) throws WeDPRException;

    DownloadCertResponse downloadCert(String certId) throws WeDPRException;

    DownloadCertToolResponse downloadCertScript() throws WeDPRException;
}
