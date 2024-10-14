package com.webank.wedpr.components.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.webank.wedpr.common.utils.PageRequest;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.user.entity.WedprPermission;
import com.webank.wedpr.components.user.requests.WedprPermissionRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * 服务类
 *
 * @author caryliao
 * @since 2024-07-15
 */
public interface WedprPermissionService extends IService<WedprPermission> {
    /** 获取权限列表 */
    WeDPRResponse listPermissionsService(PageRequest pageRequest, HttpServletRequest request);

    /** 创建新权限 */
    WeDPRResponse createPermissionService(
            WedprPermissionRequest wedprPermissionRequest, HttpServletRequest request);

    /** 获取指定权限Id详情 */
    WeDPRResponse getPermissionDetailsService(String permissionId, HttpServletRequest request);

    /** 更新指定权限Id详情 */
    WeDPRResponse updatePermissionService(
            String permissionId,
            WedprPermissionRequest wedprPermissionRequest,
            HttpServletRequest request);
}
