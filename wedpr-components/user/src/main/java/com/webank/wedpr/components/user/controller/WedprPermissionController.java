package com.webank.wedpr.components.user.controller;

import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.PageRequest;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.token.auth.TokenUtils;
import com.webank.wedpr.components.token.auth.model.UserToken;
import com.webank.wedpr.components.user.helper.PermissionHelper;
import com.webank.wedpr.components.user.requests.WedprPermissionRequest;
import com.webank.wedpr.components.user.service.WedprPermissionService;
import com.webank.wedpr.components.user.service.WedprRolePermissionService;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 权限管理表
 *
 * @author zachma
 * @since 2024-07-17
 */
@RestController
@RequestMapping(
        path = Constant.WEDPR_API_PREFIX + "/admin",
        produces = {"application/json"})
@Slf4j
public class WedprPermissionController {
    @Autowired private WedprPermissionService wedprPermissionService;

    @Autowired private WedprRolePermissionService wedprRolePermissionService;

    // 获取权限列表
    @GetMapping("/permissions")
    public WeDPRResponse listPermissionsController(
            PageRequest pageRequest, HttpServletRequest request) {
        return wedprPermissionService.listPermissionsService(pageRequest, request);
    }

    // 创建新权限
    @PostMapping("/permissions")
    public WeDPRResponse createPermissionController(
            @RequestBody WedprPermissionRequest wedprPermissionRequest,
            HttpServletRequest request) {
        return wedprPermissionService.createPermissionService(wedprPermissionRequest, request);
    }

    // 获取指定权限详情
    @GetMapping("/permissions/{permissionId}")
    public WeDPRResponse getPermissionDetailsController(
            @PathVariable String permissionId, HttpServletRequest request) {
        return wedprPermissionService.getPermissionDetailsService(permissionId, request);
    }

    // 更新指定权限信息
    @PutMapping("/permissions/{permissionId}")
    public WeDPRResponse updatePermissionController(
            @Required @PathVariable String permissionId,
            @RequestBody WedprPermissionRequest wedprPermissionRequest,
            HttpServletRequest request) {
        return wedprPermissionService.updatePermissionService(
                permissionId, wedprPermissionRequest, request);
    }

    // 删除指定权限
    @DeleteMapping("/permissions/{permissionId}")
    public WeDPRResponse deletePermissionController(
            @PathVariable String permissionId, HttpServletRequest request) {
        try {
            UserToken userToken = TokenUtils.getLoginUser(request);
            PermissionHelper.checkAdminRole(userToken);
            if (wedprRolePermissionService.isPermissionAssignToRoleService(permissionId)) {
                return new WeDPRResponse(Constant.WEDPR_FAILED, "删除权限失败,权限已被分配给角色");
            }
            boolean result = wedprPermissionService.removeById(permissionId);
            if (result) {
                return new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
            } else {
                return new WeDPRResponse(Constant.WEDPR_FAILED, "删除权限失败");
            }
        } catch (Exception e) {
            log.error("删除权限保存失败", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, "删除权限保存失败:" + e.getMessage());
        }
    }
}
