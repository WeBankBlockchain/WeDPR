package com.webank.wedpr.components.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.PageRequest;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.token.auth.TokenUtils;
import com.webank.wedpr.components.token.auth.model.UserToken;
import com.webank.wedpr.components.user.entity.WedprPermission;
import com.webank.wedpr.components.user.entity.WedprRolePermission;
import com.webank.wedpr.components.user.helper.PermissionHelper;
import com.webank.wedpr.components.user.requests.WedprRolePermissionRequest;
import com.webank.wedpr.components.user.service.WedprPermissionService;
import com.webank.wedpr.components.user.service.WedprRolePermissionService;
import com.webank.wedpr.components.user.service.WedprUserRoleService;
import com.webank.wedpr.components.uuid.generator.WeDPRUuidGenerator;
import java.time.LocalDateTime;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 角色权限关联管理
 *
 * @author zachma
 * @since 2024-07-17
 */
@RestController
@RequestMapping(
        path = Constant.WEDPR_API_PREFIX + "/admin",
        produces = {"application/json"})
@Slf4j
public class WedprRolePermissionController {

    @Autowired private WedprRolePermissionService wedprRolePermissionService;

    @Autowired private WedprPermissionService wedprPermissionService;

    @Autowired private WedprUserRoleService wedprUserRoleService;

    // 为角色分配权限
    @PostMapping("/roles/{roleId}/permissions/{permissionId}")
    public WeDPRResponse assignPermissionToRoleController(
            @Required @PathVariable String roleId,
            @Required @PathVariable String permissionId,
            HttpServletRequest request) {
        try {
            UserToken userToken = TokenUtils.getLoginUser(request);
            PermissionHelper.checkAdminRole(userToken);
            WedprPermission wedprPermission = wedprPermissionService.getById(permissionId);
            if (Objects.isNull(wedprPermission)) {
                return new WeDPRResponse(Constant.WEDPR_FAILED, "权限ID不存在");
            }

            return wedprRolePermissionService.updateWedprRolePermissionByRoleIdService(
                    userToken, roleId, permissionId);
        } catch (Exception e) {
            log.error("为角色分配权限失败", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, "为角色分配权限失败: " + e.getMessage());
        }
    }

    // 移除角色的权限
    @DeleteMapping("/roles/{roleId}/permissions/{permissionId}")
    public WeDPRResponse removePermissionFromRoleController(
            @Required @PathVariable String roleId,
            @Required @PathVariable String permissionId,
            HttpServletRequest request) {
        return wedprRolePermissionService.removePermissionFromRoleService(
                permissionId, roleId, request);
    }

    // 获取角色列表
    @GetMapping("/roles")
    public WeDPRResponse listsRolesController(PageRequest pageRequest, HttpServletRequest request) {
        return wedprRolePermissionService.listsRolesService(pageRequest, request);
    }

    // 获取指定角色列表
    @GetMapping("/roles/{roleId}")
    public WeDPRResponse getPermissionDetailsController(
            @Required @PathVariable String roleId,
            PageRequest pageRequest,
            HttpServletRequest request) {
        try {
            PermissionHelper.checkAdminRole(TokenUtils.getLoginUser(request));
            return wedprRolePermissionService.getPermissionDetailService(roleId, pageRequest);
        } catch (Exception e) {
            log.error("获取指定角色列表失败", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, "获取指定角色列表失败: " + e.getMessage());
        }
    }

    // 创建角色
    @PostMapping("/roles")
    public WeDPRResponse createRoleController(
            @Valid @RequestBody WedprRolePermissionRequest wedprRolePermissionRequest,
            HttpServletRequest request) {

        try {
            UserToken userToken = TokenUtils.getLoginUser(request);
            PermissionHelper.checkAdminRole(userToken);

            WedprRolePermission queryWedprRolePermission =
                    wedprRolePermissionService.getRolePermissionFromRolenameService(
                            wedprRolePermissionRequest.getRoleName());
            if (Objects.nonNull(queryWedprRolePermission)) {
                return new WeDPRResponse(Constant.WEDPR_FAILED, "角色名已存在");
            }
            WedprPermission wedprPermission =
                    wedprPermissionService.getById(wedprRolePermissionRequest.getPermissionId());
            if (Objects.isNull(wedprPermission)) {
                return new WeDPRResponse(Constant.WEDPR_FAILED, "权限ID不存在");
            }
            String permissionId = wedprRolePermissionRequest.getPermissionId();
            WedprRolePermission wedprRolePermission =
                    WedprRolePermission.builder()
                            .roleId(WeDPRUuidGenerator.generateID())
                            .roleName(wedprRolePermissionRequest.getRoleName())
                            .permissionId(permissionId)
                            .createBy(userToken.getUsername())
                            .updateBy("")
                            .createTime(LocalDateTime.now())
                            .build();
            wedprRolePermissionService.save(wedprRolePermission);
            return new WeDPRResponse(
                    Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG, wedprRolePermission);
        } catch (Exception e) {
            log.error("创建角色保存失败", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, "创建角色保存失败: " + e.getMessage());
        }
    }

    // 更新指定角色信息
    @PutMapping("/roles/{roleId}")
    public WeDPRResponse updateRoleController(
            @Required @PathVariable String roleId,
            @RequestBody WedprRolePermissionRequest wedprRolePermissionRequest,
            HttpServletRequest request) {

        return wedprRolePermissionService.updateRoleService(
                roleId, wedprRolePermissionRequest, request);
    }

    // 清理指定角色
    @DeleteMapping("/roles/{roleId}")
    public WeDPRResponse deleteRoleController(
            @Required @PathVariable String roleId, HttpServletRequest request) {
        try {
            PermissionHelper.checkAdminRole(TokenUtils.getLoginUser(request));
            if (wedprUserRoleService.isRoleAssignToUserService(roleId)) {
                return new WeDPRResponse(Constant.WEDPR_FAILED, "删除角色失败,角色已被分配给用户");
            }

            LambdaQueryWrapper<WedprRolePermission> wrapper =
                    new LambdaQueryWrapper<WedprRolePermission>()
                            .eq(WedprRolePermission::getRoleId, roleId);
            boolean remove = wedprRolePermissionService.remove(wrapper);
            if (remove) {
                return new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
            } else {
                return new WeDPRResponse(Constant.WEDPR_FAILED, "清理指定角色失败");
            }
        } catch (Exception e) {
            log.error("清理指定角色失败", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, "清理指定角色失败： " + e.getMessage());
        }
    }
}
