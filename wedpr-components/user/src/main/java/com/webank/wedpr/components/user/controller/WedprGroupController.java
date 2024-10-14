package com.webank.wedpr.components.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.webank.wedpr.common.protocol.UserRoleEnum;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.hook.UserHook;
import com.webank.wedpr.components.token.auth.TokenUtils;
import com.webank.wedpr.components.token.auth.model.GroupInfo;
import com.webank.wedpr.components.token.auth.model.UserToken;
import com.webank.wedpr.components.user.config.UserInfoUpdateEvent;
import com.webank.wedpr.components.user.entity.WedprGroup;
import com.webank.wedpr.components.user.entity.WedprGroupDetail;
import com.webank.wedpr.components.user.entity.WedprUser;
import com.webank.wedpr.components.user.requests.*;
import com.webank.wedpr.components.user.response.*;
import com.webank.wedpr.components.user.service.WedprGroupDetailService;
import com.webank.wedpr.components.user.service.WedprGroupService;
import com.webank.wedpr.components.user.service.WedprUserRoleService;
import com.webank.wedpr.components.user.service.WedprUserService;
import com.webank.wedpr.components.uuid.generator.WeDPRUuidGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 前端控制器
 *
 * @author caryliao
 * @since 2024-07-15
 */
@RestController
@RequestMapping(
        path = Constant.WEDPR_API_PREFIX + "/admin",
        produces = {"application/json"})
@Slf4j
public class WedprGroupController {

    @Autowired private WedprGroupService wedprGroupService;

    @Autowired private WedprGroupDetailService wedprGroupDetailService;

    @Autowired private WedprUserService wedprUserService;

    @Autowired private WedprUserRoleService wedprUserRoleService;

    @Autowired private ApplicationEventPublisher applicationEventPublisher;
    @Autowired private UserHook userHook;

    /**
     * 创建用户组，检查用户组名是否存在，创建用户组失败时记录警告日志
     *
     * @param createWedprGroupRequest 创建用户组请求参数
     * @return WeDPRResponse 返回结果
     */
    @PostMapping("/groups")
    public WeDPRResponse createGroup(
            @Valid @RequestBody CreateWedprGroupRequest createWedprGroupRequest,
            HttpServletRequest request) {
        try {
            // 检查用户权限
            UserToken userToken = TokenUtils.getLoginUser(request);
            String username = userToken.getUsername();
            if (!userToken.isAdmin()) {
                log.info("无权限创建用户组, 用户名：{}， 角色：{}", username, userToken.getRoleName());
                return new WeDPRResponse(Constant.WEDPR_FAILED, "无权限创建用户组");
            }
            // 检查用户组名是否存在
            LambdaQueryWrapper<WedprGroup> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            String groupName = createWedprGroupRequest.getGroupName();
            lambdaQueryWrapper.eq(WedprGroup::getGroupName, groupName);
            WedprGroup queriedWedprGroup = wedprGroupService.getOne(lambdaQueryWrapper);
            if (queriedWedprGroup != null) {
                return new WeDPRResponse(Constant.WEDPR_FAILED, "用户组名称已存在");
            }

            WedprGroup wedprGroup = new WedprGroup();
            wedprGroup.setGroupId(WeDPRUuidGenerator.generateID());
            wedprGroup.setGroupName(groupName);
            wedprGroup.setCreateBy(username);
            wedprGroup.setUpdateBy(username);
            wedprGroupService.save(wedprGroup);

            WeDPRResponse wedprResponse =
                    new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
            CreateWedprGroupResponse createWedprGroupResponse = new CreateWedprGroupResponse();
            createWedprGroupResponse.setGroupId(wedprGroup.getGroupId());
            wedprResponse.setData(createWedprGroupResponse);
            return wedprResponse;
        } catch (Exception e) {
            log.warn("创建用户组失败", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, "创建用户组失败:" + e.getMessage());
        }
    }

    /**
     * 删除用户组
     *
     * @param groupId
     * @return
     */
    @DeleteMapping("/groups/{groupId}")
    @Transactional
    public WeDPRResponse deleteGroup(
            @PathVariable("groupId") String groupId, HttpServletRequest request) {
        try {
            if (Constant.DEFAULT_INIT_GROUP_ID.equals(groupId)) {
                return new WeDPRResponse(Constant.WEDPR_FAILED, "默认用户组不能删除");
            }
            // 检查用户权限
            UserToken userToken = TokenUtils.getLoginUser(request);
            if (!userToken.isAdmin()) {
                log.info(
                        "无权限删除用户组, 用户名：{}， 角色：{}",
                        userToken.getUsername(),
                        userToken.getRoleName());
                return new WeDPRResponse(Constant.WEDPR_FAILED, "无权限删除用户组");
            }

            // 检查用户组是否存在
            if (!checkGroupExistByGroupId(groupId)) {
                return new WeDPRResponse(Constant.WEDPR_FAILED, "用户组不存在");
            }
            publicUserInfoUpdateEvent(groupId);
            LambdaQueryWrapper<WedprGroupDetail> lambdaQueryWrapper =
                    new LambdaQueryWrapper<WedprGroupDetail>()
                            .eq(WedprGroupDetail::getGroupId, groupId);
            wedprGroupService.removeById(groupId);
            wedprGroupDetailService.remove(lambdaQueryWrapper);
            WeDPRResponse wedprResponse =
                    new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
            CreateWedprGroupResponse createWedprGroupResponse = new CreateWedprGroupResponse();
            createWedprGroupResponse.setGroupId(groupId);
            wedprResponse.setData(createWedprGroupResponse);

            return wedprResponse;
        } catch (Exception e) {
            log.warn("删除用户组失败", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new WeDPRResponse(Constant.WEDPR_FAILED, "删除用户组失败:" + e.getMessage());
        }
    }

    private boolean checkGroupExistByGroupId(String groupId) {
        LambdaQueryWrapper<WedprGroup> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(WedprGroup::getGroupId, groupId);
        WedprGroup queriedWedprGroup = wedprGroupService.getOne(lambdaQueryWrapper);
        return queriedWedprGroup != null;
    }

    /**
     * 新增用户到用户组，检查用户组是否存在，用户是否已在群组中，新增用户失败时记录警告日志
     *
     * @param groupId 用户组ID
     * @param addGroupUserRequest 添加用户请求参数
     * @return WeDPRResponse 返回结果
     */
    @PostMapping("/groups/{groupId}/users")
    public WeDPRResponse addGroupUsers(
            @PathVariable("groupId") String groupId,
            @Valid @RequestBody AddGroupUserRequest addGroupUserRequest,
            HttpServletRequest request) {
        try {
            // 检查用户权限,需要机构管理或者群组管理员才可以新增用户
            UserToken userToken = TokenUtils.getLoginUser(request);
            if (!hasAgencyAdminOrGroupUser(groupId, userToken, true)) {
                log.info(
                        "无权限新增用户, 用户名：{}， 角色：{}", userToken.getUsername(), userToken.getRoleName());
                return new WeDPRResponse(Constant.WEDPR_FAILED, "无权限新增用户");
            }

            // 检查用户组是否存在
            if (!checkGroupExistByGroupId(groupId)) {
                return new WeDPRResponse(Constant.WEDPR_FAILED, "用户组不存在");
            }
            WeDPRResponse wedprResponse =
                    new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
            List<WedprUser> wedprUserList = addGroupUserRequest.getWedprUserList();
            List<WedprGroupDetail> wedprGroupDetailList = new ArrayList<>(wedprUserList.size());
            List<String> usernameList = new ArrayList<>();
            for (WedprUser wedprUser : wedprUserList) {
                // 检查用户是否存在
                LambdaQueryWrapper<WedprUser> lambdaQueryWrapper1 = new LambdaQueryWrapper<>();
                String username = wedprUser.getUsername().trim();
                lambdaQueryWrapper1.eq(WedprUser::getUsername, username);
                if (wedprUserService.getOne(lambdaQueryWrapper1) == null) {
                    return new WeDPRResponse(Constant.WEDPR_FAILED, "用户不存在");
                }
                WedprGroupDetail newWedprGroupDetail = new WedprGroupDetail();
                newWedprGroupDetail.setGroupId(groupId);
                newWedprGroupDetail.setUsername(wedprUser.getUsername());
                newWedprGroupDetail.setCreateBy(userToken.getUsername());
                newWedprGroupDetail.setUpdateBy(userToken.getUsername());
                wedprGroupDetailList.add(newWedprGroupDetail);
                usernameList.add(wedprUser.getUsername());
            }
            wedprGroupDetailService.saveBatch(wedprGroupDetailList);
            CreateWedprGroupResponse createWedprGroupResponse = new CreateWedprGroupResponse();
            createWedprGroupResponse.setGroupId(groupId);
            wedprResponse.setData(createWedprGroupResponse);

            applicationEventPublisher.publishEvent(new UserInfoUpdateEvent<>(usernameList));

            return wedprResponse;
        } catch (Exception e) {
            log.warn("群组新增用户失败", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new WeDPRResponse(Constant.WEDPR_FAILED, "群组新增用户失败");
        }
    }

    private boolean hasAgencyAdminOrGroupUser(
            String requestGroupId, UserToken userToken, boolean hasNeedGroupAdmin) {
        // 判断用户是否是机构理员
        if (UserRoleEnum.ADMIN_ROLE.getRoleName().equals(userToken.getRoleName())) {
            return true;
        }
        List<GroupInfo> groupInfos = userToken.getGroupInfos();
        for (GroupInfo groupInfo : groupInfos) {
            boolean hasGroupUser = requestGroupId.equals(groupInfo.getGroupId());
            if (hasNeedGroupAdmin) {
                // 判断用户是否是用户组管理员
                if (hasGroupUser && userToken.getUsername().equals(groupInfo.getGroupAdminName())) {
                    return true;
                }
            } else {
                // 判断用户是否群组用户
                if (hasGroupUser) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 设置用户组管理员，检查用户组是否存在，用户是否在群组，新增管理员失败时记录警告日志
     *
     * @param createAdminUserRequest 创建管理员请求参数
     * @return WeDPRResponse 返回结果
     */
    @PatchMapping("/groups/adminuser")
    public WeDPRResponse setGroupAdminUser(
            @Valid @RequestBody CreateAdminUserRequest createAdminUserRequest,
            HttpServletRequest request) {
        try {
            // 检查用户权限,需要机构管理或者群组管理员
            UserToken userToken = TokenUtils.getLoginUser(request);
            if (!hasAgencyAdminOrGroupUser(createAdminUserRequest.getGroupId(), userToken, true)) {
                log.info(
                        "无权限变更群组管理员, 用户名：{}， 角色：{}",
                        userToken.getUsername(),
                        userToken.getRoleName());
                return new WeDPRResponse(Constant.WEDPR_FAILED, "无权限变更群组管理员");
            }
            // 检查用户组是否存在
            String groupId = createAdminUserRequest.getGroupId();
            String username = createAdminUserRequest.getUsername();
            if (!checkGroupExistByGroupId(groupId)) {
                return new WeDPRResponse(Constant.WEDPR_FAILED, "用户组不存在");
            }

            // 检查用户是否在群组
            LambdaQueryWrapper<WedprGroupDetail> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(WedprGroupDetail::getGroupId, groupId);
            lambdaQueryWrapper.eq(WedprGroupDetail::getUsername, username);
            WedprGroupDetail wedprGroupDetail = wedprGroupDetailService.getOne(lambdaQueryWrapper);
            if (wedprGroupDetail == null) {
                return new WeDPRResponse(
                        Constant.WEDPR_FAILED, "用户" + username + "不存在用户组:" + groupId);
            }
            WeDPRResponse wedprResponse =
                    new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
            WedprGroup wedprGroup = wedprGroupService.getById(groupId);
            wedprGroup.setAdminName(username);
            wedprGroup.setUpdateBy(userToken.getUsername());
            wedprGroupService.saveOrUpdate(wedprGroup);
            CreateWedprGroupResponse createWedprGroupResponse = new CreateWedprGroupResponse();
            createWedprGroupResponse.setGroupId(groupId);
            wedprResponse.setData(createWedprGroupResponse);

            publicUserInfoUpdateEvent(groupId);

            return wedprResponse;
        } catch (Exception e) {
            log.warn("新增管理员失败", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, "新增管理员失败:" + e.getMessage());
        }
    }

    private void publicUserInfoUpdateEvent(String groupId) {
        LambdaQueryWrapper<WedprGroupDetail> lambdaQueryWrapper =
                new LambdaQueryWrapper<WedprGroupDetail>()
                        .eq(WedprGroupDetail::getGroupId, groupId);
        List<WedprGroupDetail> wedprGroupDetailList =
                wedprGroupDetailService.list(lambdaQueryWrapper);
        List<String> usernameList =
                wedprGroupDetailList
                        .stream()
                        .map(wedprGroupDetail1 -> wedprGroupDetail1.getUsername())
                        .collect(Collectors.toList());
        applicationEventPublisher.publishEvent(new UserInfoUpdateEvent<>(usernameList));
    }

    /**
     * 删除用户组中的用户
     *
     * @param groupId
     * @param username
     * @return
     */
    @DeleteMapping("/groups/{groupId}/users/{username}")
    public WeDPRResponse deleteGroupUser(
            @PathVariable("groupId") String groupId,
            @PathVariable("username") String username,
            HttpServletRequest request) {
        try {
            // 检查用户权限,需要机构管理或者群组管理员
            UserToken userToken = TokenUtils.getLoginUser(request);
            if (!hasAgencyAdminOrGroupUser(groupId, userToken, true)) {
                log.info(
                        "无权限删除用户组中的用户, 用户名：{}， 角色：{}",
                        userToken.getUsername(),
                        userToken.getRoleName());
                return new WeDPRResponse(Constant.WEDPR_FAILED, "无权限删除用户组中的用户");
            }
            // 检查用户组是否存在
            if (!checkGroupExistByGroupId(groupId)) {
                return new WeDPRResponse(Constant.WEDPR_FAILED, "用户组不存在");
            }
            // 检查用户名是否存在
            LambdaQueryWrapper<WedprGroupDetail> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(WedprGroupDetail::getGroupId, groupId);
            lambdaQueryWrapper.eq(WedprGroupDetail::getUsername, username);
            WedprGroupDetail wedprGroupDetail = wedprGroupDetailService.getOne(lambdaQueryWrapper);
            if (wedprGroupDetail == null) {
                return new WeDPRResponse(Constant.WEDPR_FAILED, "用户不在用户组中");
            }
            publicUserInfoUpdateEvent(groupId);
            WeDPRResponse wedprResponse =
                    new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
            LambdaQueryWrapper<WedprGroupDetail> lambdaQueryWrapper2 = new LambdaQueryWrapper<>();
            lambdaQueryWrapper2.eq(WedprGroupDetail::getUsername, username);
            wedprGroupDetailService.remove(lambdaQueryWrapper2);

            // call userHook
            userHook.onUserDeleted(username);

            CreateWedprGroupResponse createWedprGroupResponse = new CreateWedprGroupResponse();
            createWedprGroupResponse.setGroupId(groupId);
            wedprResponse.setData(createWedprGroupResponse);

            return wedprResponse;
        } catch (Exception e) {
            log.warn("删除用户失败", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, "删除用户失败:" + e.getMessage());
        }
    }

    /**
     * 查询用户组列表，查询失败时记录警告日志
     *
     * @param getGroupRequest 查询用户组请求参数
     * @return WeDPRResponse 返回结果
     */
    @GetMapping("/groups")
    public WeDPRResponse getGroupList(
            @Valid GetGroupRequest getGroupRequest, HttpServletRequest request) {
        try {
            // 检查用户权限,需要机构管理
            UserToken userToken = TokenUtils.getLoginUser(request);
            WeDPRResponse wedprResponse =
                    new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
            LambdaQueryWrapper<WedprGroup> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            String groupId = getGroupRequest.getGroupId();
            if (UserRoleEnum.ADMIN_ROLE.getRoleName().equals(userToken.getRoleName())) {
                // 是机构管理员，查询所有群组
                if (!StringUtils.isEmpty(groupId)) {
                    lambdaQueryWrapper.like(WedprGroup::getGroupId, groupId);
                }
            } else {
                // 是普通用户，查询自己所在群组的列表
                List<GroupInfo> groupInfos = userToken.getGroupInfos();
                List<String> groupIds =
                        groupInfos
                                .stream()
                                .map(groupInfo -> groupInfo.getGroupId())
                                .collect(Collectors.toList());
                lambdaQueryWrapper.in(groupInfos.size() > 0, WedprGroup::getGroupId, groupIds);
            }
            String groupName = getGroupRequest.getGroupName();
            if (!StringUtils.isEmpty(groupName)) {
                lambdaQueryWrapper.like(WedprGroup::getGroupName, groupName);
            }
            lambdaQueryWrapper.orderByDesc(WedprGroup::getCreateTime);
            Page<WedprGroup> pageWedprGroup =
                    new Page<>(getGroupRequest.getPageNum(), getGroupRequest.getPageSize());
            Page<WedprGroup> page = wedprGroupService.page(pageWedprGroup, lambdaQueryWrapper);
            GetWedprGroupListResponse getWedprGroupListResponse = new GetWedprGroupListResponse();
            getWedprGroupListResponse.setTotal(page.getTotal());
            List<WedprGroup> wedprGroupList = page.getRecords();
            List<WedprGroupDTO> wedprGroupDTOList = new ArrayList<>(wedprGroupList.size());
            for (WedprGroup wedprGroup : wedprGroupList) {
                int userCount =
                        wedprGroupDetailService.count(
                                new LambdaQueryWrapper<WedprGroupDetail>()
                                        .eq(WedprGroupDetail::getGroupId, wedprGroup.getGroupId()));
                WedprGroupDTO wedprGroupDTO = new WedprGroupDTO();
                wedprGroupDTO.setUserCount(userCount);
                wedprGroupDTO.setGroupId(wedprGroup.getGroupId());
                wedprGroupDTO.setGroupName(wedprGroup.getGroupName());
                wedprGroupDTO.setAdminName(wedprGroup.getAdminName());
                wedprGroupDTO.setCreateTime(wedprGroup.getCreateTime());
                wedprGroupDTO.setUpdateTime(wedprGroup.getUpdateTime());
                wedprGroupDTO.setCreateBy(wedprGroup.getCreateBy());
                wedprGroupDTO.setUpdateBy(wedprGroup.getUpdateBy());
                wedprGroupDTO.setStatus(wedprGroup.getStatus());
                wedprGroupDTOList.add(wedprGroupDTO);
            }
            getWedprGroupListResponse.setGroupList(wedprGroupDTOList);
            wedprResponse.setData(getWedprGroupListResponse);
            return wedprResponse;
        } catch (Exception e) {
            log.warn("查询用户组列表失败", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, "查询用户组列表失败:" + e.getMessage());
        }
    }

    /**
     * 查询用户组详情
     *
     * @param groupId
     * @param getGroupDetailRequest
     * @return
     */
    @GetMapping("/groups/{groupId}")
    public WeDPRResponse getGroupDetail(
            @PathVariable("groupId") String groupId,
            @Valid GetGroupDetailRequest getGroupDetailRequest,
            HttpServletRequest request) {
        try {
            // 检查用户权限,需要机构管理或者群组管理员
            UserToken userToken = TokenUtils.getLoginUser(request);
            if (!hasAgencyAdminOrGroupUser(groupId, userToken, false)) {
                log.info(
                        "无权限查询用户组详情, 用户名：{}， 角色：{}",
                        userToken.getUsername(),
                        userToken.getRoleName());
                return new WeDPRResponse(Constant.WEDPR_FAILED, "无权限查询用户组详情");
            }
            WeDPRResponse wedprResponse =
                    new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
            String username = getGroupDetailRequest.getUsername();
            Page<WedprUserResponse> page =
                    new Page<>(
                            getGroupDetailRequest.getPageNum(),
                            getGroupDetailRequest.getPageSize());
            IPage<WedprUserResponse> wedprGroupIPage =
                    wedprGroupDetailService.selectGroupUserPage(page, groupId, username);
            GetWedprGroupDetailResponse getWedprGroupListDTO = new GetWedprGroupDetailResponse();
            getWedprGroupListDTO.setTotal(wedprGroupIPage.getTotal());
            getWedprGroupListDTO.setUserList(wedprGroupIPage.getRecords());
            wedprResponse.setData(getWedprGroupListDTO);
            return wedprResponse;
        } catch (Exception e) {
            log.warn("查询群组详情失败", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, "查询群组详情失败:" + e.getMessage());
        }
    }

    @GetMapping("/groupCount")
    public WeDPRResponse getGroupCount(HttpServletRequest request) {
        try {
            UserToken userToken = TokenUtils.getLoginUser(request);
            String roleName = userToken.getRoleName();
            int groupCount = 0;
            if (userToken.isAdmin()) {
                groupCount = wedprGroupService.count();
            } else {
                List<GroupInfo> groupInfos = userToken.getGroupInfos();
                for (GroupInfo groupInfo : groupInfos) {
                    if (userToken.getUsername().equals(groupInfo.getGroupAdminName())) {
                        groupCount++;
                    }
                }
            }
            WeDPRResponse wedprResponse =
                    new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
            GetGroupCountResponse getGroupCountResponse = new GetGroupCountResponse();
            getGroupCountResponse.setGroupCount(groupCount);
            wedprResponse.setData(getGroupCountResponse);
            return wedprResponse;
        } catch (Exception e) {
            log.warn("查询用户组数量失败", e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, "查询用户组数量失败:" + e.getMessage());
        }
    }
}
