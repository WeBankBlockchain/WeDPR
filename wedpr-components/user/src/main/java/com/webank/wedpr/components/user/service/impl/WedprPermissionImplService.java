package com.webank.wedpr.components.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.PageRequest;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.token.auth.TokenUtils;
import com.webank.wedpr.components.token.auth.model.UserToken;
import com.webank.wedpr.components.user.entity.WedprPermission;
import com.webank.wedpr.components.user.helper.PermissionHelper;
import com.webank.wedpr.components.user.mapper.WedprPermissionMapper;
import com.webank.wedpr.components.user.requests.WedprPermissionRequest;
import com.webank.wedpr.components.user.response.WedprPermissionResponse;
import com.webank.wedpr.components.user.service.WedprPermissionService;
import com.webank.wedpr.components.uuid.generator.WeDPRUuidGenerator;
import java.time.LocalDateTime;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 服务实现类
 *
 * @author caryliao
 * @since 2024-07-15
 */
@Service
@Slf4j
public class WedprPermissionImplService extends ServiceImpl<WedprPermissionMapper, WedprPermission>
        implements WedprPermissionService {
    @Override
    public WeDPRResponse listPermissionsService(
            PageRequest pageRequest, HttpServletRequest request) {
        try {
            PermissionHelper.checkAdminRole(TokenUtils.getLoginUser(request));
            Page<WedprPermission> pageWedprPermission =
                    new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
            Page<WedprPermission> page = this.page(pageWedprPermission);
            return new WeDPRResponse(
                    Constant.WEDPR_SUCCESS,
                    Constant.WEDPR_SUCCESS_MSG,
                    new WedprPermissionResponse(page.getTotal(), page.getRecords()));
        } catch (Exception e) {
            log.warn("查询权限列表失败", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, "查询权限列表失败:" + e.getMessage());
        }
    }

    @Override
    public WeDPRResponse createPermissionService(
            WedprPermissionRequest wedprPermissionRequest, HttpServletRequest request) {
        try {
            UserToken userToken = TokenUtils.getLoginUser(request);
            PermissionHelper.checkAdminRole(userToken);
            if (StringUtils.isEmpty(wedprPermissionRequest.getPermissionName())) {
                return new WeDPRResponse(Constant.WEDPR_FAILED, "权限名不能为空");
            }

            LambdaQueryWrapper<WedprPermission> wrapper =
                    new LambdaQueryWrapper<WedprPermission>()
                            .eq(
                                    WedprPermission::getPermissionName,
                                    wedprPermissionRequest.getPermissionName());
            WedprPermission queriedWedprPermission = this.getOne(wrapper);
            if (Objects.nonNull(queriedWedprPermission)) {
                return new WeDPRResponse(Constant.WEDPR_FAILED, "权限名已存在");
            }

            WedprPermission wedprPermission =
                    WedprPermission.builder()
                            .permissionId(WeDPRUuidGenerator.generateID())
                            .permissionName(wedprPermissionRequest.getPermissionName())
                            .permissionContent(wedprPermissionRequest.getPermissionContent())
                            .createBy(userToken.getUsername())
                            .updateBy("")
                            .createTime(LocalDateTime.now())
                            .build();
            this.save(wedprPermission);
            return new WeDPRResponse(
                    Constant.WEDPR_SUCCESS,
                    Constant.WEDPR_SUCCESS_MSG,
                    wedprPermission.getPermissionId());
        } catch (Exception e) {
            log.error("创建权限保存失败", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, "创建权限保存失败: " + e.getMessage());
        }
    }

    @Override
    public WeDPRResponse getPermissionDetailsService(
            String permissionId, HttpServletRequest request) {
        try {
            PermissionHelper.checkAdminRole(TokenUtils.getLoginUser(request));
            WedprPermission wedprPermission = this.getById(permissionId);
            if (Objects.isNull(wedprPermission)) {
                return new WeDPRResponse(Constant.WEDPR_FAILED, "权限名未添加");
            }
            return new WeDPRResponse(
                    Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG, wedprPermission);
        } catch (Exception e) {
            log.warn("查询权限列表失败", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, "查询权限列表失败:" + e.getMessage());
        }
    }

    @Override
    public WeDPRResponse updatePermissionService(
            String permissionId,
            WedprPermissionRequest wedprPermissionRequest,
            HttpServletRequest request) {
        try {
            // 需要机构管理员权限
            UserToken userToken = TokenUtils.getLoginUser(request);
            PermissionHelper.checkAdminRole(userToken);
            LambdaQueryWrapper<WedprPermission> wrapper =
                    new LambdaQueryWrapper<WedprPermission>()
                            .eq(WedprPermission::getPermissionId, permissionId);
            WedprPermission queriedWedprPermission = this.getOne(wrapper);
            if (Objects.isNull(queriedWedprPermission)) {
                return new WeDPRResponse(Constant.WEDPR_FAILED, "权限不存在");
            }

            WedprPermission.WedprPermissionBuilder builder =
                    WedprPermission.builder()
                            .updateBy(userToken.getUsername())
                            .updateTime(LocalDateTime.now());

            if (!StringUtils.isEmpty(wedprPermissionRequest.getPermissionName())) {
                builder.permissionName(wedprPermissionRequest.getPermissionName());
            }
            if (!StringUtils.isEmpty(wedprPermissionRequest.getPermissionContent())) {
                builder.permissionContent(wedprPermissionRequest.getPermissionContent());
            }
            WedprPermission wedprPermission = builder.build();
            this.update(wedprPermission, wrapper);
            return new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        } catch (Exception e) {
            log.error("更新权限保存失败", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, "更新权限保存失败: " + e.getMessage());
        }
    }
}
