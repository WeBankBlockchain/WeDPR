package com.webank.wedpr.components.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webank.wedpr.components.admin.common.Utils;
import com.webank.wedpr.components.admin.config.WedprCertConfig;
import com.webank.wedpr.components.admin.entity.WedprAgency;
import com.webank.wedpr.components.admin.entity.WedprCert;
import com.webank.wedpr.components.admin.mapper.WedprCertMapper;
import com.webank.wedpr.components.admin.service.LocalShellService;
import com.webank.wedpr.components.admin.service.WedprAgencyService;
import com.webank.wedpr.components.admin.service.WedprCertService;
import com.webank.wedpr.core.utils.Constant;
import com.webank.wedpr.core.utils.WeDPRException;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
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
    public String createOrUpdateCert(HttpServletRequest request, String username)
            throws WeDPRException, IOException {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        String certId = multipartRequest.getParameter("certId");
        String agencyId = multipartRequest.getParameter("agencyId");
        String expireTimeStr = multipartRequest.getParameter("expireTime");
        LocalDateTime expireTime = Utils.getLocalDateTime(expireTimeStr);
        long days = Utils.getDaysDifference(expireTime);
        if (days <= 0) {
            throw new WeDPRException("expireTime is invalid");
        }
        WedprAgency wedprAgency = wedprAgencyService.getById(agencyId);
        if (wedprAgency == null) {
            throw new WeDPRException("agency does not exist");
        }
        String agencyName = wedprAgency.getAgencyName();
        MultipartFile multipartFile = multipartRequest.getFile("csrFile");
        if (multipartFile == null) {
            throw new WeDPRException("Please provide agency csr file");
        }
        // 获取文件名/
        String filename = multipartFile.getOriginalFilename();
        if (!Utils.isSafeCommand(filename)) {
            throw new WeDPRException("filename is unSafe.");
        }
        String csrPath =
                wedprCertConfig.getAgencyCertPath()
                        + File.separator
                        + agencyName
                        + File.separator
                        + filename;
        File csrFile = new File(csrPath);
        if (!csrFile.exists()) {
            csrFile.mkdirs();
        }
        multipartFile.transferTo(csrFile);
        String csrFileText = Utils.fileToBase64(csrFile.toPath().toString());
        boolean result = localShellService.buildAuthorityCsrToCrt(agencyName, csrPath, days);
        if (!result) {
            throw new WeDPRException("create or update agency cert error");
        }
        String crtFileStr =
                handleCrtFile(
                        wedprCertConfig.getAgencyCertPath() + File.separator + agencyName,
                        agencyName);
        if (StringUtils.isEmpty(certId)) {
            WedprCert wedprCert = new WedprCert();
            wedprCert.setAgencyId(agencyId);
            wedprCert.setAgencyName(agencyName);
            wedprCert.setCsrFileText(csrFileText);
            wedprCert.setCertFileText(crtFileStr);
            wedprCert.setExpireTime(expireTime);
            wedprCert.setCreateBy(username);
            wedprCert.setUpdateBy(username);
            save(wedprCert);
            return wedprCert.getCertId();
        } else {
            WedprCert wedprCert = getById(certId);
            if (wedprCert == null) {
                throw new WeDPRException("cert does not exist");
            }
            wedprCert.setCsrFileText(csrFileText);
            wedprCert.setCertFileText(crtFileStr);
            wedprCert.setExpireTime(expireTime);
            wedprCert.setUpdateBy(username);
            wedprCert.setUpdateTime(LocalDateTime.now());
            updateById(wedprCert);
            return certId;
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
