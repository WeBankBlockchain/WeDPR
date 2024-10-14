package com.webank.wedpr.components.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.token.auth.TokenUtils;
import com.webank.wedpr.components.token.auth.model.UserToken;
import com.webank.wedpr.components.user.entity.WedprUser;
import com.webank.wedpr.components.user.entity.WedprUserRole;
import com.webank.wedpr.components.user.entity.result.WedprUserRoleResult;
import com.webank.wedpr.components.user.helper.PermissionHelper;
import com.webank.wedpr.components.user.service.WedprRolePermissionService;
import com.webank.wedpr.components.user.service.WedprUserRoleService;
import com.webank.wedpr.components.user.service.WedprUserService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户角色关联管理
 *
 * @author zachma
 * @since 2024-07-17
 */
@RestController
@RequestMapping(Constant.WEDPR_API_PREFIX + "/admin")
@Slf4j
public class WedprUserRoleController {

    @Autowired private WedprUserRoleService wedprUserRoleService;

    @Autowired private WedprUserService wedprUserService;

    @Autowired private WedprRolePermissionService wedprRolePermissionService;

    // 为用户分配角色
    @PostMapping("/users/{userName}/roles/{roleId}")
    public WeDPRResponse assignRoleToUserController(
            @Required @PathVariable String userName,
            @Required @PathVariable String roleId,
            HttpServletRequest request) {
        try {
            UserToken userToken = TokenUtils.getLoginUser(request);
            PermissionHelper.checkAdminRole(userToken);

            WedprUser queriedWedprUser = wedprUserService.getWedprUserByNameService(userName);
            if (Objects.isNull(queriedWedprUser)) {
                return new WeDPRResponse(Constant.WEDPR_FAILED, "用户名不存在");
            }

            if (!wedprRolePermissionService.isRolePermissionContainRoleIdService(roleId)) {
                return new WeDPRResponse(Constant.WEDPR_FAILED, "角色Id不存在");
            }

            WedprUserRole wedprUserRole =
                    WedprUserRole.builder()
                            .roleId(roleId)
                            .username(userName)
                            .createBy(userToken.getUsername())
                            .updateBy("")
                            .createTime(LocalDateTime.now())
                            .build();
            wedprUserRoleService.save(wedprUserRole);
            return new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        } catch (Exception e) {
            log.error("为用户分配角色失败", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, "为用户分配角色失败：" + e.getMessage());
        }
    }

    // 获取用户的角色信息
    @GetMapping("/users/roles/{userName}")
    public WeDPRResponse getRoleFromUserController(@Required @PathVariable String userName) {
        List<WedprUserRoleResult> wedprUserRoleByUserName =
                wedprUserRoleService.getWedprUserRoleByUsername(userName);
        return new WeDPRResponse(
                Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG, wedprUserRoleByUserName);
    }

    // 移除用户的角色
    @DeleteMapping("/users/{userName}/roles/{roleId}")
    public WeDPRResponse removeRoleFromUserController(
            @Required @PathVariable String userName,
            @Required @PathVariable String roleId,
            HttpServletRequest request) {
        try {
            PermissionHelper.checkAdminRole(TokenUtils.getLoginUser(request));
            LambdaQueryWrapper<WedprUserRole> lambdaWedprUserRoleQueryWrapper =
                    new LambdaQueryWrapper<WedprUserRole>()
                            .eq(WedprUserRole::getUsername, userName)
                            .eq(WedprUserRole::getRoleId, roleId);
            boolean removed = wedprUserRoleService.remove(lambdaWedprUserRoleQueryWrapper);
            if (removed) {
                return new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
            } else {
                return new WeDPRResponse(Constant.WEDPR_FAILED, "移除用户的角色失败");
            }
        } catch (Exception e) {
            log.error("移除用户的角色失败", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, "移除用户的角色失败: " + e.getMessage());
        }
    }
}
