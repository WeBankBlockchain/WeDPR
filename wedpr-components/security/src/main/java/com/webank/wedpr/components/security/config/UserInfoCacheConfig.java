package com.webank.wedpr.components.security.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.webank.wedpr.components.token.auth.model.GroupInfo;
import com.webank.wedpr.components.user.config.UserInfoUpdateEvent;
import com.webank.wedpr.components.user.config.UserJwtConfig;
import com.webank.wedpr.components.user.entity.WedprGroup;
import com.webank.wedpr.components.user.entity.WedprGroupDetail;
import com.webank.wedpr.components.user.entity.result.WedprUserRoleResult;
import com.webank.wedpr.components.user.service.WedprGroupDetailService;
import com.webank.wedpr.components.user.service.WedprGroupService;
import com.webank.wedpr.components.user.service.WedprUserRoleService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.StringUtils;

/** Created by caryliao on 2024/8/12 22:03 */
@Configuration
@Slf4j
public class UserInfoCacheConfig {
    @Autowired private WedprUserRoleService wedprUserRoleService;
    @Autowired private WedprGroupDetailService wedprGroupDetailService;
    @Autowired private WedprGroupService wedprGroupService;
    @Autowired private UserJwtConfig userJwtConfig;

    @Bean
    public LoadingCache<String, UserJwtInfo> makeUserInfoCache() {
        // 创建CacheLoader
        CacheLoader<String, UserJwtInfo> loader =
                new CacheLoader<String, UserJwtInfo>() {
                    @Override
                    public UserJwtInfo load(String username) {
                        // 从用户服务获取用户信息
                        log.info("从数据库查询用户信息：{}", username);
                        return getUpdatedUserJwtInfo(username);
                    }
                };
        // 创建LoadingCache
        LoadingCache<String, UserJwtInfo> loadingCache =
                CacheBuilder.newBuilder().maximumSize(userJwtConfig.getCacheSize()).build(loader);
        return loadingCache;
    }

    private UserJwtInfo getUpdatedUserJwtInfo(String username) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        getAuthorities(username, authorities);
        String newRoleName =
                authorities
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(userJwtConfig.getDelimiter()));
        LambdaQueryWrapper<WedprGroupDetail> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(WedprGroupDetail::getUsername, username);
        List<WedprGroupDetail> wedprGroupDetailList =
                wedprGroupDetailService.list(lambdaQueryWrapper);
        List<GroupInfo> newGroupInfos = new ArrayList<>(wedprGroupDetailList.size());
        for (WedprGroupDetail wedprGroupDetail : wedprGroupDetailList) {
            GroupInfo groupInfo = new GroupInfo();
            String groupId = wedprGroupDetail.getGroupId();
            groupInfo.setGroupId(groupId);
            WedprGroup wedprGroup = wedprGroupService.getById(groupId);
            groupInfo.setGroupName(wedprGroup.getGroupName());
            groupInfo.setGroupAdminName(wedprGroup.getAdminName());
            newGroupInfos.add(groupInfo);
        }
        UserJwtInfo userJwtInfo = new UserJwtInfo(newRoleName, newGroupInfos);
        return userJwtInfo;
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

    @EventListener
    public void listenUserInfoEvent(UserInfoUpdateEvent<List> userInfoUpdateEvent) {
        LoadingCache<String, UserJwtInfo> loadingCache = makeUserInfoCache();
        loadingCache.invalidateAll(userInfoUpdateEvent.getData());
        log.info("需要更新的缓存用户：" + userInfoUpdateEvent.getData());
    }
}
