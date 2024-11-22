package com.webank.wedpr.components.security.config;

import com.webank.wedpr.components.security.cache.UserCache;
import com.webank.wedpr.components.security.cache.impl.MemoryUserCache;
import com.webank.wedpr.components.token.auth.model.UserJwtConfig;
import com.webank.wedpr.components.user.config.UserInfoUpdateEvent;
import com.webank.wedpr.components.user.service.WedprGroupDetailService;
import com.webank.wedpr.components.user.service.WedprGroupService;
import com.webank.wedpr.components.user.service.WedprUserRoleService;
import com.webank.wedpr.components.user.service.WedprUserService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;

/** Created by caryliao on 2024/8/12 22:03 */
@Configuration
@Slf4j
public class UserInfoCacheConfig {
    @Autowired private WedprUserRoleService wedprUserRoleService;
    @Autowired private WedprGroupDetailService wedprGroupDetailService;
    @Autowired private WedprGroupService wedprGroupService;
    @Autowired private WedprUserService wedprUserService;
    private final UserJwtConfig userJwtConfig = new UserJwtConfig();

    @Bean(name = "userCache")
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    @ConditionalOnMissingBean
    public UserCache userCache() {
        return new MemoryUserCache(
                wedprUserRoleService,
                wedprGroupDetailService,
                wedprGroupService,
                wedprUserService,
                userJwtConfig);
    }

    @EventListener
    public void listenUserInfoEvent(UserInfoUpdateEvent<List> userInfoUpdateEvent) {
        UserCache userCache = userCache();
        userCache.invalidateAll(userInfoUpdateEvent);
        log.info("需要更新的缓存用户：" + userInfoUpdateEvent.getData());
    }
}
