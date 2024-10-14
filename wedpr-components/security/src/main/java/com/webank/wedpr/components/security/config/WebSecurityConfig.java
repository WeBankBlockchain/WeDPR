package com.webank.wedpr.components.security.config;

import com.webank.wedpr.common.protocol.ServerTypeEnum;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.components.api.credential.core.CredentialVerifier;
import com.webank.wedpr.components.security.cache.UserCache;
import com.webank.wedpr.components.security.filter.APISignatureAuthFilter;
import com.webank.wedpr.components.security.filter.JwtAuthenticationFilter;
import com.webank.wedpr.components.security.filter.JwtLoginFilter;
import com.webank.wedpr.components.user.config.UserJwtConfig;
import com.webank.wedpr.components.user.service.WedprGroupDetailService;
import com.webank.wedpr.components.user.service.WedprGroupService;
import com.webank.wedpr.components.user.service.WedprUserService;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired private WedprUserDetailService wedprUserDetailService;

    @Autowired private WedprUserService wedprUserService;

    @Lazy @Resource private AuthenticationManager authenticationManager;

    @Autowired private UserJwtConfig userJwtConfig;

    @Autowired private WedprGroupDetailService wedprGroupDetailService;

    @Autowired private WedprGroupService wedprGroupService;

    @Autowired private UserCache userCache;
    @Autowired private CredentialVerifier credentialVerifier;

    @Value("${server.type:site_end}")
    private String serverType;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 设置登录验证服务类
     *
     * @param auth
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 设置UserDetailsService的实现类
        auth.userDetailsService(wedprUserDetailService);
    }

    /**
     * 配置哪些请求不拦截 排除swagger相关请求
     *
     * @param web
     * @throws Exception
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .antMatchers(
                        "/favicon.ico",
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/v2/**",
                        "/v3/**",
                        "/swagger-ui/**",
                        "/doc.html",
                        Constant.REGISTER_URL,
                        Constant.USER_PUBLICKEY_URL,
                        Constant.IMAGE_CODE_URL);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        String loginUrl = getLoginUrl(serverType);
        JwtLoginFilter jwtLoginFilter =
                new JwtLoginFilter(
                        authenticationManager,
                        userJwtConfig,
                        wedprUserService,
                        userCache,
                        loginUrl);
        JwtAuthenticationFilter jwtAuthenticationFilter =
                new JwtAuthenticationFilter(authenticationManager, userJwtConfig, userCache);
        // the api credential filter
        APISignatureAuthFilter apiSignatureAuthFilter =
                new APISignatureAuthFilter(authenticationManager, credentialVerifier, userCache);

        // the filter order is: jwtLoginFilter, jwtAuthenticationFilter, APISignatureAuthFilter
        http.cors()
                .and()
                .csrf()
                .disable()
                .authorizeRequests()
                .antMatchers("/**")
                .permitAll()
                .and()
                .headers()
                .frameOptions()
                .sameOrigin()
                .and()
                .formLogin()
                .loginProcessingUrl(loginUrl)
                .and()
                .addFilter(jwtLoginFilter)
                .addFilter(jwtAuthenticationFilter)
                .addFilter(apiSignatureAuthFilter)
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    public static String getLoginUrl(String serverType) {
        String loginUrl = Constant.SITE_END_LOGIN_URL;
        if (ServerTypeEnum.ADMIN_END.getName().equals(serverType)) {
            loginUrl = Constant.ADMIN_END_LOGIN_URL;
        }
        return loginUrl;
    }
}
