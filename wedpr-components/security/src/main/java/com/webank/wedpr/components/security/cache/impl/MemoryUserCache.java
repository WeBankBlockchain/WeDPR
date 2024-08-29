/*
 * Copyright 2017-2025  [webank-wedpr]
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.webank.wedpr.components.security.cache.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.webank.wedpr.components.security.cache.UserCache;
import com.webank.wedpr.components.token.auth.TokenUtils;
import com.webank.wedpr.components.token.auth.model.GroupInfo;
import com.webank.wedpr.components.token.auth.model.UserToken;
import com.webank.wedpr.components.user.config.UserInfoUpdateEvent;
import com.webank.wedpr.components.user.config.UserJwtConfig;
import com.webank.wedpr.components.user.entity.WedprGroup;
import com.webank.wedpr.components.user.entity.WedprGroupDetail;
import com.webank.wedpr.components.user.entity.result.WedprUserRoleResult;
import com.webank.wedpr.components.user.service.WedprGroupDetailService;
import com.webank.wedpr.components.user.service.WedprGroupService;
import com.webank.wedpr.components.user.service.WedprUserRoleService;
import com.webank.wedpr.components.user.service.WedprUserService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.StringUtils;

public class MemoryUserCache implements UserCache {
    private static final Logger logger = LoggerFactory.getLogger(MemoryUserCache.class);

    private final WedprUserRoleService wedprUserRoleService;
    private final WedprGroupDetailService wedprGroupDetailService;
    private final WedprGroupService wedprGroupService;
    private final WedprUserService wedprUserService;
    private final UserJwtConfig userJwtConfig;
    // Note: LoadingCache is thread-safe
    private LoadingCache<String, Optional<UserToken>> userCache;

    public MemoryUserCache(
            WedprUserRoleService wedprUserRoleService,
            WedprGroupDetailService wedprGroupDetailService,
            WedprGroupService wedprGroupService,
            WedprUserService wedprUserService,
            UserJwtConfig userJwtConfig) {
        this.wedprUserRoleService = wedprUserRoleService;
        this.wedprGroupDetailService = wedprGroupDetailService;
        this.wedprGroupService = wedprGroupService;
        this.wedprUserService = wedprUserService;
        this.userJwtConfig = userJwtConfig;

        // create CacheLoader
        CacheLoader<String, Optional<UserToken>> loader =
                new CacheLoader<String, Optional<UserToken>>() {
                    @Override
                    public Optional<UserToken> load(String username) {
                        logger.info("从数据库查询用户信息：{}", username);
                        // check the existence of user
                        UserToken userToken = null;
                        if (wedprUserService.getWedprUserByNameService(username) != null) {
                            userToken = fetchUserToken(username);
                        }
                        return Optional.ofNullable(userToken);
                    }
                };
        // 创建LoadingCache
        userCache =
                CacheBuilder.newBuilder().maximumSize(CacheConfig.getUserCacheSize()).build(loader);
    }

    @Override
    public UserToken getUserToken(HttpServletRequest request) throws Exception {
        UserToken userToken = TokenUtils.getLoginUser(request);
        String username = userToken.getUsername();
        Optional<UserToken> latestUserToken = userCache.get(username);
        // the user not exists
        if (!latestUserToken.isPresent()) {
            return null;
        }
        boolean hasRoleNameUpdate =
                !userToken.getRoleName().equals(latestUserToken.get().getRoleName());
        boolean hasGroupInfoUpdate =
                !CollectionUtils.isEqualCollection(
                        userToken.getGroupInfos(), latestUserToken.get().getGroupInfos());
        if (hasRoleNameUpdate || hasGroupInfoUpdate) {
            userToken.setRoleName(latestUserToken.get().getRoleName());
            userToken.setGroupInfos(latestUserToken.get().getGroupInfos());
        }
        return userToken;
    }

    @Override
    public UserToken getUserToken(String userName) throws Exception {
        wedprUserService.updateAllowedTimeAndTryCount(userName, 0L, 0);
        Optional<UserToken> userToken = userCache.get(userName);
        return userToken.orElse(null);
    }

    @Override
    public void invalidateAll(UserInfoUpdateEvent<List> userInfoUpdateEvent) {
        userCache.invalidateAll(userInfoUpdateEvent.getData());
    }

    private UserToken fetchUserToken(String username) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        getAuthorities(username, authorities);
        String roleName =
                authorities
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(userJwtConfig.getDelimiter()));
        LambdaQueryWrapper<WedprGroupDetail> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(WedprGroupDetail::getUsername, username);
        List<WedprGroupDetail> wedprGroupDetailList =
                wedprGroupDetailService.list(lambdaQueryWrapper);
        List<GroupInfo> groupInfos = new ArrayList<>(wedprGroupDetailList.size());
        for (WedprGroupDetail wedprGroupDetail : wedprGroupDetailList) {
            GroupInfo groupInfo = new GroupInfo();
            String groupId = wedprGroupDetail.getGroupId();
            groupInfo.setGroupId(groupId);
            WedprGroup wedprGroup = wedprGroupService.getById(groupId);
            groupInfo.setGroupName(wedprGroup.getGroupName());
            groupInfo.setGroupAdminName(wedprGroup.getAdminName());
            groupInfos.add(groupInfo);
        }
        return new UserToken(username, roleName, groupInfos);
    }

    private void getAuthorities(String username, List<GrantedAuthority> authorities) {
        List<WedprUserRoleResult> wedprUserRoleResultList =
                wedprUserRoleService.getWedprUserRoleByUsername(username);
        wedprUserRoleResultList.forEach(
                wedprUserRoleResult -> {
                    if (wedprUserRoleResult != null
                            && !StringUtils.isEmpty(wedprUserRoleResult.getRoleName())) {
                        GrantedAuthority grantedAuthority =
                                new SimpleGrantedAuthority(wedprUserRoleResult.getRoleName());
                        authorities.add(grantedAuthority);
                    }
                });
    }
}
