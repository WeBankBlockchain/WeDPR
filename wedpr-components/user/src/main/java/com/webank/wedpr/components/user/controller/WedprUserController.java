package com.webank.wedpr.components.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.webank.wedpr.common.config.WeDPRCommonConfig;
import com.webank.wedpr.common.protocol.UserRoleEnum;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.token.auth.TokenUtils;
import com.webank.wedpr.components.token.auth.model.GroupInfo;
import com.webank.wedpr.components.token.auth.model.UserToken;
import com.webank.wedpr.components.user.config.UserJwtConfig;
import com.webank.wedpr.components.user.entity.WedprUser;
import com.webank.wedpr.components.user.entity.WedprUserRole;
import com.webank.wedpr.components.user.helper.PasswordHelper;
import com.webank.wedpr.components.user.requests.GetUserListRequest;
import com.webank.wedpr.components.user.requests.UpdatePasswordRequest;
import com.webank.wedpr.components.user.requests.UpdateUserInfoRequest;
import com.webank.wedpr.components.user.response.GetUserAgencyResponse;
import com.webank.wedpr.components.user.response.GetUserCountResponse;
import com.webank.wedpr.components.user.response.GetUserInfoResponse;
import com.webank.wedpr.components.user.response.GetUserListResponse;
import com.webank.wedpr.components.user.response.WedprUserDTO;
import com.webank.wedpr.components.user.service.WedprGroupDetailService;
import com.webank.wedpr.components.user.service.WedprUserRoleService;
import com.webank.wedpr.components.user.service.WedprUserService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前端控制器
 *
 * @author caryliao
 * @since 2024-07-15
 */
@RestController
@RequestMapping(
        path = Constant.WEDPR_API_PREFIX,
        produces = {"application/json"})
@Slf4j
public class WedprUserController {
    @Autowired private WedprUserService wedprUserService;
    @Autowired private WedprUserRoleService wedprUserRoleService;
    @Autowired private WedprGroupDetailService wedprGroupDetailService;
    @Autowired private UserJwtConfig userJwtConfig;

    /**
     * 查询本机构用户列表
     *
     * @param getUserListRequest
     * @return
     */
    @GetMapping("/users")
    public WeDPRResponse getUserList(@Valid GetUserListRequest getUserListRequest) {
        try {
            WeDPRResponse wedprResponse =
                    new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
            String username = getUserListRequest.getUsername();
            LambdaQueryWrapper<WedprUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            if (StringUtils.hasLength(username)) {
                lambdaQueryWrapper.like(WedprUser::getUsername, username);
            }
            Page<WedprUser> wedprUserPage =
                    new Page<>(getUserListRequest.getPageNum(), getUserListRequest.getPageSize());
            Page<WedprUser> page = wedprUserService.page(wedprUserPage, lambdaQueryWrapper);
            GetUserListResponse getUserListResponse = new GetUserListResponse();
            List<WedprUser> records = page.getRecords();
            ArrayList<WedprUserDTO> wedprUserDTOList = new ArrayList<>();
            for (WedprUser record : records) {
                WedprUserDTO wedprUserDTO = new WedprUserDTO();
                wedprUserDTO.setUsername(record.getUsername());
                wedprUserDTOList.add(wedprUserDTO);
            }
            getUserListResponse.setUserList(wedprUserDTOList);
            getUserListResponse.setTotal(page.getTotal());
            wedprResponse.setData(getUserListResponse);
            return wedprResponse;
        } catch (Exception e) {
            log.warn("查询用户列表失败", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, "查询用户列表失败:" + e.getMessage());
        }
    }

    @GetMapping("/users-info")
    public WeDPRResponse getUserInfo(HttpServletRequest request) {
        try {
            UserToken userToken = TokenUtils.getLoginUser(request);
            WeDPRResponse wedprResponse =
                    new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
            WedprUser wedprUser =
                    wedprUserService.getOne(
                            new LambdaQueryWrapper<WedprUser>()
                                    .eq(WedprUser::getUsername, userToken.getUsername()));
            GetUserInfoResponse getUserInfoResponse = new GetUserInfoResponse();
            getUserInfoResponse.setUsername(wedprUser.getUsername());
            getUserInfoResponse.setPhone(wedprUser.getPhone());
            getUserInfoResponse.setEmail(wedprUser.getEmail());
            wedprResponse.setData(getUserInfoResponse);
            return wedprResponse;
        } catch (Exception e) {
            log.warn("更新用户信息失败", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, "更新用户信息失败:" + e.getMessage());
        }
    }

    @PatchMapping("/users-info")
    public WeDPRResponse updateUserInfo(
            @Valid @RequestBody UpdateUserInfoRequest updateUserInfoRequest,
            HttpServletRequest request) {
        try {
            UserToken userToken = TokenUtils.getLoginUser(request);
            WeDPRResponse wedprResponse =
                    new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
            WedprUser wedprUser =
                    wedprUserService.getOne(
                            new LambdaQueryWrapper<WedprUser>()
                                    .eq(WedprUser::getUsername, userToken.getUsername()));
            wedprUser.setPhone(updateUserInfoRequest.getPhone());
            wedprUser.setEmail(updateUserInfoRequest.getEmail());
            wedprUser.setUpdateBy(userToken.getUsername());
            wedprUserService.updateById(wedprUser);
            return wedprResponse;
        } catch (Exception e) {
            log.warn("更新用户信息失败", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, "更新用户信息失败:" + e.getMessage());
        }
    }

    @PatchMapping("/users-password")
    public WeDPRResponse updateUserPassword(
            @Valid @RequestBody UpdatePasswordRequest updatePasswordRequest,
            HttpServletRequest request) {
        try {
            UserToken userToken = TokenUtils.getLoginUser(request);
            WeDPRResponse wedprResponse =
                    new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            String oldCipherPassword = updatePasswordRequest.getOldPassword();
            String oldPassword =
                    PasswordHelper.decryptPassword(
                            oldCipherPassword, userJwtConfig.getPrivateKey());
            WedprUser wedprUser =
                    wedprUserService.getOne(
                            new LambdaQueryWrapper<WedprUser>()
                                    .eq(WedprUser::getUsername, userToken.getUsername()));
            if (!bCryptPasswordEncoder.matches(
                    oldPassword,
                    wedprUser.getPassword().substring(Constant.ENCRYPT_PREFIX.length()))) {
                return new WeDPRResponse(Constant.WEDPR_FAILED, "原密码错误，请重新输入");
            }
            String newCipherPassword = updatePasswordRequest.getNewPassword();
            String newPassword =
                    PasswordHelper.decryptPassword(
                            newCipherPassword, userJwtConfig.getPrivateKey());
            if (PasswordHelper.isStrongNonValid(newPassword)) {
                return new WeDPRResponse(Constant.WEDPR_FAILED, "密码强度不够，请重新输入");
            }
            wedprUser.setPassword(
                    Constant.ENCRYPT_PREFIX + bCryptPasswordEncoder.encode(newPassword));
            wedprUser.setUpdateBy(userToken.getUsername());
            wedprUserService.updateById(wedprUser);
            return wedprResponse;
        } catch (Exception e) {
            log.warn("更新用户密码失败", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, "更新用户密码失败:" + e.getMessage());
        }
    }

    @GetMapping("/userCount")
    public WeDPRResponse getUserCount(HttpServletRequest request) {
        try {
            UserToken userToken = TokenUtils.getLoginUser(request);
            String roleName = userToken.getRoleName();
            int userCount = 0;
            if (UserRoleEnum.ADMIN_ROLE.getRoleName().equals(roleName)) {
                userCount = wedprUserService.count();
            } else {
                List<GroupInfo> groupInfos = userToken.getGroupInfos();
                List<String> adminGroupIds = new ArrayList<>();
                for (GroupInfo groupInfo : groupInfos) {
                    String groupId = groupInfo.getGroupId();
                    if (userToken.getUsername().equals(groupInfo.getGroupAdminName())) {
                        adminGroupIds.add(groupId);
                    }
                }
                if (adminGroupIds.isEmpty()) {
                    // 普通用户，返回管理的用户数为0
                    userCount = 0;
                } else {
                    userCount = wedprGroupDetailService.selectUserCount(adminGroupIds);
                }
            }
            WeDPRResponse wedprResponse =
                    new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
            GetUserCountResponse getUserCountResponse = new GetUserCountResponse();
            getUserCountResponse.setUserCount(userCount);
            wedprResponse.setData(getUserCountResponse);
            return wedprResponse;
        } catch (Exception e) {
            log.warn("查询用户数失败", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, "查询用户数失败:" + e.getMessage());
        }
    }

    @GetMapping("/userAgency")
    public WeDPRResponse getUserAgency() {
        try {
            WeDPRResponse wedprResponse =
                    new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
            GetUserAgencyResponse getUserAgencyResponse = new GetUserAgencyResponse();

            LambdaQueryWrapper<WedprUserRole> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(WedprUserRole::getRoleId, UserRoleEnum.ADMIN_ROLE.getRoleId());
            List<WedprUserRole> wedprUserRoleList = wedprUserRoleService.list(lambdaQueryWrapper);
            String agencyAdminName =
                    wedprUserRoleList
                            .stream()
                            .map(WedprUserRole::getUsername)
                            .collect(Collectors.joining(Constant.STR_SEPARATOR));
            getUserAgencyResponse.setAgencyAdminName(agencyAdminName);
            getUserAgencyResponse.setAgencyName(WeDPRCommonConfig.getAgency());
            wedprResponse.setData(getUserAgencyResponse);
            return wedprResponse;
        } catch (Exception e) {
            log.warn("查询机构信息失败", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, "查询机构信息失败:" + e.getMessage());
        }
    }
}
