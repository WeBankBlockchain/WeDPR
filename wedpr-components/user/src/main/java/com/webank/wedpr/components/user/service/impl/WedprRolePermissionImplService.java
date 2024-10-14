package com.webank.wedpr.components.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.PageRequest;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.token.auth.TokenUtils;
import com.webank.wedpr.components.token.auth.model.UserToken;
import com.webank.wedpr.components.user.entity.WedprRolePermission;
import com.webank.wedpr.components.user.entity.result.WedprRolePermissionResult;
import com.webank.wedpr.components.user.helper.PermissionHelper;
import com.webank.wedpr.components.user.mapper.WedprRolePermissionMapper;
import com.webank.wedpr.components.user.requests.WedprRolePermissionRequest;
import com.webank.wedpr.components.user.response.WedprRolePermissionResponse;
import com.webank.wedpr.components.user.service.WedprRolePermissionService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

/**
 * 服务实现类
 *
 * @author caryliao
 * @since 2024-07-15
 */
@Service
public class WedprRolePermissionImplService
        extends ServiceImpl<WedprRolePermissionMapper, WedprRolePermission>
        implements WedprRolePermissionService {

    @Override
    public IPage<WedprRolePermissionResult> selectRolePermissionPage(
            Page<WedprRolePermissionResult> page, String roleId) {
        List<WedprRolePermissionResult> wedprRolePermissionResults = null;
        if (roleId == null) {
            wedprRolePermissionResults = this.baseMapper.selectRolePermissionAllPage(page);
        } else {
            wedprRolePermissionResults =
                    this.baseMapper.selectRolePermissionPageByRoleId(page, roleId);
        }
        page.setRecords(wedprRolePermissionResults);
        return page;
    }

    @Override
    public Boolean isPermissionAssignToRoleService(String permissionId) {
        LambdaQueryWrapper<WedprRolePermission> wrapper =
                new LambdaQueryWrapper<WedprRolePermission>()
                        .eq(WedprRolePermission::getPermissionId, permissionId);
        int queriedWedprPermissionCount = this.count(wrapper);
        return queriedWedprPermissionCount > 0;
    }

    @Override
    public WeDPRResponse updateWedprRolePermissionByRoleIdService(
            UserToken userToken, String roleId, String permissionId) {
        LambdaQueryWrapper<WedprRolePermission> wrapper =
                new LambdaQueryWrapper<WedprRolePermission>()
                        .eq(WedprRolePermission::getRoleId, roleId);
        WedprRolePermission queriedWedprRolePermission = this.getOne(wrapper);
        if (Objects.isNull(queriedWedprRolePermission)) {
            return new WeDPRResponse(Constant.WEDPR_FAILED, "角色名不存在");
        }

        WedprRolePermission wedprRolePermission =
                WedprRolePermission.builder()
                        .permissionId(permissionId)
                        .updateBy(userToken.getUsername())
                        .updateTime(LocalDateTime.now())
                        .build();
        this.update(wedprRolePermission, wrapper);
        return new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
    }

    @Override
    public WeDPRResponse removePermissionFromRoleService(
            String permissionId, String roleId, HttpServletRequest request) {
        try {
            PermissionHelper.checkAdminRole(TokenUtils.getLoginUser(request));
            LambdaQueryWrapper<WedprRolePermission> wrapper =
                    new LambdaQueryWrapper<WedprRolePermission>()
                            .eq(WedprRolePermission::getRoleId, roleId)
                            .eq(WedprRolePermission::getPermissionId, permissionId);
            boolean result = this.remove(wrapper);
            if (result) {
                return new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
            } else {
                return new WeDPRResponse(Constant.WEDPR_FAILED, "移除角色权限失败");
            }
        } catch (Exception e) {
            log.error("为角色移除权限失败", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, "为角色移除权限失败: " + e.getMessage());
        }
    }

    @Override
    public WeDPRResponse listsRolesService(PageRequest pageRequest, HttpServletRequest request) {
        try {
            PermissionHelper.checkAdminRole(TokenUtils.getLoginUser(request));
            Page<WedprRolePermissionResult> pageWedprPermission =
                    new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
            IPage<WedprRolePermissionResult> wedprRolePermissionResultPage =
                    this.selectRolePermissionPage(pageWedprPermission, null);
            return new WeDPRResponse(
                    Constant.WEDPR_SUCCESS,
                    Constant.WEDPR_SUCCESS_MSG,
                    new WedprRolePermissionResponse(
                            wedprRolePermissionResultPage.getTotal(),
                            wedprRolePermissionResultPage.getRecords()));
        } catch (Exception e) {
            log.error("获取角色列表失败", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, "获取角色列表失败: " + e.getMessage());
        }
    }

    @Override
    public WeDPRResponse getPermissionDetailService(String roleId, PageRequest pageRequest) {
        Page<WedprRolePermissionResult> pageWedprPermission =
                new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        IPage<WedprRolePermissionResult> wedprRolePermissionResultPage =
                this.selectRolePermissionPage(pageWedprPermission, roleId);
        return new WeDPRResponse(
                Constant.WEDPR_SUCCESS,
                Constant.WEDPR_SUCCESS_MSG,
                new WedprRolePermissionResponse(
                        wedprRolePermissionResultPage.getTotal(),
                        wedprRolePermissionResultPage.getRecords()));
    }

    @Override
    public WedprRolePermission getRolePermissionFromRolenameService(String rolename) {
        LambdaQueryWrapper<WedprRolePermission> lambdaQueryWrapper =
                new LambdaQueryWrapper<WedprRolePermission>()
                        .eq(WedprRolePermission::getRoleName, rolename);
        return this.getOne(lambdaQueryWrapper);
    }

    @Override
    public Boolean isRolePermissionContainRoleIdService(String roleId) {
        LambdaQueryWrapper<WedprRolePermission> lambdaRoleIdQueryWrapper =
                new LambdaQueryWrapper<WedprRolePermission>()
                        .eq(WedprRolePermission::getRoleId, roleId);
        int queriedWedprRolePermissionCount = this.count(lambdaRoleIdQueryWrapper);
        return queriedWedprRolePermissionCount > 0;
    }

    @Override
    public WeDPRResponse updateRoleService(
            String roleId,
            WedprRolePermissionRequest wedprRolePermissionRequest,
            HttpServletRequest request) {
        try {
            UserToken userToken = TokenUtils.getLoginUser(request);
            PermissionHelper.checkAdminRole(userToken);
            LambdaQueryWrapper<WedprRolePermission> wrapper =
                    new LambdaQueryWrapper<WedprRolePermission>()
                            .eq(WedprRolePermission::getRoleId, roleId);
            WedprRolePermission wedprRolePermission =
                    WedprRolePermission.builder()
                            .roleName(wedprRolePermissionRequest.getRoleName())
                            .updateBy(userToken.getUsername())
                            .updateTime(LocalDateTime.now())
                            .build();
            this.update(wedprRolePermission, wrapper);
            return new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        } catch (Exception e) {
            log.error("更新角色信息失败", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, "更新角色信息失败： " + e.getMessage());
        }
    }
}
