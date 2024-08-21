package com.webank.wedpr.components.security.config;

import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

/**
 * @author shareong
 * @date 2022/12/2
 */
@Configuration
public class AuthenticationProviderConfig {
    @Resource WedprUserDetailService wedprUserDetailService;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(wedprUserDetailService);
        return authProvider;
    }
}
