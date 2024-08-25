package com.webank.wedpr.components.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.webank.wedpr.components.admin.entity.WedprCert;
import com.webank.wedpr.core.utils.WeDPRException;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;

/**
 * 服务类
 *
 * @author caryliao
 * @since 2024-08-22
 */
public interface WedprCertService extends IService<WedprCert> {
    String createOrUpdateCert(HttpServletRequest request, String username)
            throws WeDPRException, IOException;
}
