package com.webank.wedpr.components.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webank.wedpr.common.protocol.CertStatusEnum;
import com.webank.wedpr.common.protocol.CertStatusViewEnum;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.admin.common.Utils;
import com.webank.wedpr.components.admin.config.WedprCertConfig;
import com.webank.wedpr.components.admin.entity.WedprAgency;
import com.webank.wedpr.components.admin.entity.WedprCert;
import com.webank.wedpr.components.admin.mapper.WedprCertMapper;
import com.webank.wedpr.components.admin.request.GetWedprCertListRequest;
import com.webank.wedpr.components.admin.request.SetAgencyCertRequest;
import com.webank.wedpr.components.admin.response.*;
import com.webank.wedpr.components.admin.service.LocalShellService;
import com.webank.wedpr.components.admin.service.WedprAgencyService;
import com.webank.wedpr.components.admin.service.WedprCertService;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * 服务实现类
 *
 * @author caryliao
 * @since 2024-08-22
 */
@Service
@Slf4j
public class WedprCertServiceImpl extends ServiceImpl<WedprCertMapper, WedprCert>
        implements WedprCertService {
    @Autowired private WedprAgencyService wedprAgencyService;

    @Autowired private WedprCertConfig wedprCertConfig;

    @Autowired private LocalShellService localShellService;

    @Override
    public String createOrUpdateAgencyCert(HttpServletRequest request, String username)
            throws WeDPRException, IOException {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        String certId = multipartRequest.getParameter("certId");
        String agencyName = multipartRequest.getParameter("agencyName");
        String expireTimeStr = multipartRequest.getParameter("expireTime");
        LocalDateTime expireTime = Utils.getLocalDateTime(expireTimeStr);
        long days = Utils.getDaysDifference(expireTime);
        if (days <= 0) {
            throw new WeDPRException("expireTime is invalid");
        }
        MultipartFile multipartFile = multipartRequest.getFile("csrFile");
        if (multipartFile == null) {
            throw new WeDPRException("Please provide agency csr file");
        }
        // 获取文件名/
        String csrFileName = multipartFile.getOriginalFilename();
        if (!Utils.isSafeCommand(csrFileName)) {
            throw new WeDPRException("csrFileName is unSafe.");
        }
        String csrPath =
                wedprCertConfig.getAgencyCertPath()
                        + File.separator
                        + agencyName
                        + File.separator
                        + csrFileName;
        File csrFile = new File(csrPath);
        if (!csrFile.exists()) {
            csrFile.mkdirs();
        }
        multipartFile.transferTo(csrFile);
        String csrFileText = Utils.fileToBase64(csrFile.toPath().toString());
        LocalDateTime now = LocalDateTime.now();
        expireTime =
                expireTime
                        .withHour(now.getHour())
                        .withMinute(now.getMinute())
                        .withSecond(now.getSecond());
        boolean result = localShellService.buildAuthorityCsrToCrt(agencyName, csrPath, days);
        if (!result) {
            throw new WeDPRException("create or update agency cert error");
        }
        String crtFileStr =
                handleCrtFile(
                        wedprCertConfig.getAgencyCertPath() + File.separator + agencyName,
                        agencyName);
        if (StringUtils.isEmpty(certId)) {
            WedprAgency wedprAgency =
                    wedprAgencyService.getOne(
                            new LambdaQueryWrapper<WedprAgency>()
                                    .eq(WedprAgency::getAgencyName, agencyName));
            if (wedprAgency == null) {
                throw new WeDPRException("agency does not exist");
            }
            WedprCert wedprCert = new WedprCert();
            wedprCert.setAgencyId(wedprAgency.getAgencyId());
            wedprCert.setAgencyName(agencyName);
            wedprCert.setCsrFileName(csrFileName);
            wedprCert.setCsrFileText(csrFileText);
            wedprCert.setCertFileText(crtFileStr);
            wedprCert.setExpireTime(expireTime);
            wedprCert.setCreateBy(username);
            wedprCert.setUpdateBy(username);
            save(wedprCert);
            return wedprCert.getCertId();
        } else {
            WedprCert wedprCert = checkAgencyCertExist(certId);
            wedprCert.setCsrFileName(csrFileName);
            wedprCert.setCsrFileText(csrFileText);
            wedprCert.setCertFileText(crtFileStr);
            wedprCert.setExpireTime(expireTime);
            wedprCert.setUpdateBy(username);
            wedprCert.setUpdateTime(now);
            updateById(wedprCert);
            return certId;
        }
    }

    private WedprCert checkAgencyCertExist(String certId) throws WeDPRException {
        WedprCert wedprCert = getById(certId);
        if (wedprCert == null) {
            throw new WeDPRException("agency cert does not exist");
        }
        return wedprCert;
    }

    @Override
    public void deleteAgencyCert(String certId) throws WeDPRException {
        WedprCert wedprCert = checkAgencyCertExist(certId);
        removeById(certId);
    }

    @Override
    public void setAgencyCert(SetAgencyCertRequest setAgencyCertRequest, String username)
            throws WeDPRException {
        WedprCert wedprCert = checkAgencyCertExist(setAgencyCertRequest.getCertId());
        wedprCert.setCertStatus(setAgencyCertRequest.getCertStatus());
        wedprCert.setUpdateBy(username);
        wedprCert.setUpdateTime(LocalDateTime.now());
        updateById(wedprCert);
    }

    @Override
    public GetWedprCertListResponse getAgencyCertList(GetWedprCertListRequest request) {
        String agencyName = request.getAgencyName();
        Integer certStatus = request.getCertStatus();
        String signStartTimeStr = request.getSignStartTime();
        String signEndTimeStr = request.getSignEndTime();
        LambdaQueryWrapper<WedprCert> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (!StringUtils.isEmpty(agencyName)) {
            lambdaQueryWrapper.like(WedprCert::getAgencyName, agencyName);
        }
        if (certStatus != null) {
            setCertStatusQueryParam(certStatus, lambdaQueryWrapper);
        }
        if (!StringUtils.isEmpty(signStartTimeStr)) {
            LocalDateTime signStartTime = Utils.getLocalDateTime(signStartTimeStr);
            lambdaQueryWrapper.ge(WedprCert::getCreateTime, signStartTime);
        }
        if (!StringUtils.isEmpty(signEndTimeStr)) {
            LocalDateTime signEndTime = Utils.getLocalDateTime(signEndTimeStr);
            lambdaQueryWrapper.le(WedprCert::getCreateTime, signEndTime);
        }
        lambdaQueryWrapper.orderByDesc(WedprCert::getUpdateTime);
        Page<WedprCert> wedprAgencyPage = new Page<>(request.getPageNum(), request.getPageSize());
        Page<WedprCert> page = page(wedprAgencyPage, lambdaQueryWrapper);
        GetWedprCertListResponse response = new GetWedprCertListResponse();
        response.setTotal(page.getTotal());
        List<WedprCert> wedprCertList = page.getRecords();
        List<WedprCertDTO> wedprCertDTOList = new ArrayList<>();
        for (WedprCert wedprCert : wedprCertList) {
            WedprCertDTO wedprCertDTO = new WedprCertDTO();
            wedprCertDTO.setCertId(wedprCert.getCertId());
            wedprCertDTO.setAgencyId(wedprCert.getAgencyId());
            wedprCertDTO.setAgencyName(wedprCert.getAgencyName());
            wedprCertDTO.setSignTime(wedprCert.getCreateTime());
            wedprCertDTO.setExpireTime(wedprCert.getExpireTime());
            Integer certStatusView =
                    Utils.getCertStatusView(wedprCert.getCertStatus(), wedprCert.getExpireTime());
            wedprCertDTO.setCertStatus(certStatusView);
            wedprCertDTO.setEnable(wedprCert.getCertStatus());
            wedprCertDTOList.add(wedprCertDTO);
        }
        response.setAgencyCertList(wedprCertDTOList);
        return response;
    }

    @Override
    public GetWedprCertDetailResponse getAgencyCsrDetail(String certId) throws WeDPRException {
        WedprCert wedprCert = checkAgencyCertExist(certId);
        GetWedprCertDetailResponse response = new GetWedprCertDetailResponse();
        response.setCertId(wedprCert.getCertId());
        response.setAgencyName(wedprCert.getAgencyName());
        response.setCsrFileName(wedprCert.getCsrFileName());
        response.setCsrFile(wedprCert.getCsrFileText());
        response.setExpireTime(wedprCert.getExpireTime());
        return response;
    }

    @Override
    public DownloadCertResponse downloadCert(String certId) throws WeDPRException {
        WedprCert wedprCert = checkAgencyCertExist(certId);
        DownloadCertResponse response = new DownloadCertResponse();
        response.setCertName(wedprCert.getAgencyName() + Constant.ZIP_FILE_SUFFIX);
        response.setCertScriptData(wedprCert.getCertFileText());
        return response;
    }

    @Override
    public DownloadCertToolResponse downloadCertScript() throws WeDPRException {
        try (InputStream inputStream =
                this.getClass().getClassLoader().getResourceAsStream(Utils.CERT_SCRIPT_NAME)) {
            if (inputStream == null) {
                log.error("cert tool file not found");
                throw new WeDPRException(Constant.WEDPR_FAILED, "cert tool file not found");
            }
            byte[] bytes = Utils.readInputStream(inputStream);
            String certToolFileData = Base64.getEncoder().encodeToString(bytes);
            DownloadCertToolResponse response = new DownloadCertToolResponse();
            response.setCertToolName(Utils.CERT_SCRIPT_NAME);
            response.setCertToolData(certToolFileData);
            return response;
        } catch (Exception e) {
            log.error("download cert tool file error", e);
            throw new WeDPRException(Constant.WEDPR_FAILED, "download cert tool file error");
        }
    }

    private static void setCertStatusQueryParam(
            Integer certStatus, LambdaQueryWrapper<WedprCert> lambdaQueryWrapper) {
        if (CertStatusViewEnum.VALID_CERT.getStatusValue() == certStatus) {
            lambdaQueryWrapper.eq(
                    WedprCert::getCertStatus, CertStatusEnum.ENABLE_CERT.getStatusValue());
            lambdaQueryWrapper.ge(WedprCert::getExpireTime, LocalDateTime.now());
        } else if (CertStatusViewEnum.EXPIRED_CERT.getStatusValue() == certStatus) {
            lambdaQueryWrapper.eq(
                    WedprCert::getCertStatus, CertStatusEnum.ENABLE_CERT.getStatusValue());
            lambdaQueryWrapper.lt(WedprCert::getExpireTime, LocalDateTime.now());
        } else if (CertStatusViewEnum.FORBID_CERT.getStatusValue() == certStatus) {
            lambdaQueryWrapper.eq(
                    WedprCert::getCertStatus, CertStatusEnum.FORBID_CERT.getStatusValue());
        } else {
            log.info("ignore query certStatus:{}", certStatus);
        }
    }

    private String handleCrtFile(String agencyCertPath, String agencyName) throws WeDPRException {
        boolean toZipResult = Utils.fileToZip(agencyCertPath, agencyName);
        if (!toZipResult) {
            log.error("toZipResult error");
            throw new WeDPRException("toZipResult error");
        }
        String crtFileStr =
                Utils.fileToBase64(
                        agencyCertPath + File.separator + agencyName + Constant.ZIP_FILE_SUFFIX);
        if (Utils.isEmpty(crtFileStr)) {
            log.error("fileToBase64 error");
            throw new WeDPRException("fileToBase64 error");
        }
        return crtFileStr;
    }
}
