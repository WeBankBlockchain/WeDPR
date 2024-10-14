package com.webank.wedpr.components.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.webank.wedpr.common.utils.PageRequest;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.token.auth.model.UserToken;
import com.webank.wedpr.components.user.entity.WedprRolePermission;
import com.webank.wedpr.components.user.entity.result.WedprRolePermissionResult;
import com.webank.wedpr.components.user.requests.WedprRolePermissionRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * 服务类
 *
 * @author caryliao
 * @since 2024-07-15
 */
public interface WedprRolePermissionService extends IService<WedprRolePermission> {
    IPage<WedprRolePermissionResult> selectRolePermissionPage(
            Page<WedprRolePermissionResult> page, String roleId);

    /** permissionId 是否被设置给其他角色 */
    Boolean isPermissionAssignToRoleService(String permissionId);

    /** 通过RoleId/PermissionId更新角色信息 */
    WeDPRResponse updateWedprRolePermissionByRoleIdService(
            UserToken userToken, String roleId, String permissionId);

    /** 移除角色的权限 permissionId 和 roleId */
    WeDPRResponse removePermissionFromRoleService(
            String permissionId, String roleId, HttpServletRequest request);

    /** 列举所有角色 */
    WeDPRResponse listsRolesService(PageRequest pageRequest, HttpServletRequest request);

    /** 获取角色详细信息 */
    WeDPRResponse getPermissionDetailService(String roleId, PageRequest pageRequest);

    /** 通过rolename获取角色权限 */
    WedprRolePermission getRolePermissionFromRolenameService(String rolename);

    Boolean isRolePermissionContainRoleIdService(String roleId);

    WeDPRResponse updateRoleService(
            String roleId,
            WedprRolePermissionRequest wedprRolePermissionRequest,
            HttpServletRequest request);
}
