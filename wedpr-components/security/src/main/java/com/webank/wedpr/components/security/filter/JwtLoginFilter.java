package com.webank.wedpr.components.security.filter;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.security.cache.UserCache;
import com.webank.wedpr.components.token.auth.TokenUtils;
import com.webank.wedpr.components.token.auth.model.HeaderInfo;
import com.webank.wedpr.components.token.auth.model.TokenContents;
import com.webank.wedpr.components.token.auth.model.UserToken;
import com.webank.wedpr.components.user.config.UserJwtConfig;
import com.webank.wedpr.components.user.entity.WedprUser;
import com.webank.wedpr.components.user.helper.PasswordHelper;
import com.webank.wedpr.components.user.requests.LoginRequest;
import com.webank.wedpr.components.user.requests.LoginResponse;
import com.webank.wedpr.components.user.service.WedprUserService;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Objects;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class JwtLoginFilter extends UsernamePasswordAuthenticationFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtLoginFilter.class);
    private static final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    private final AuthenticationManager authenticationManager;
    private final UserJwtConfig userJwtConfig;
    private final WedprUserService wedprUserService;
    private final UserCache userCache;

    public JwtLoginFilter(
            AuthenticationManager authenticationManager,
            UserJwtConfig userJwtConfig,
            WedprUserService wedprUserService,
            UserCache userCache,
            String loginUrl) {
        super.setFilterProcessesUrl(loginUrl);
        this.authenticationManager = authenticationManager;
        this.userJwtConfig = userJwtConfig;
        this.wedprUserService = wedprUserService;
        this.userCache = userCache;
    }

    String getRequestBodyString(HttpServletRequest request) throws Exception {
        BufferedReader br = request.getReader();
        String tmp;
        StringBuilder ret = new StringBuilder();
        while ((tmp = br.readLine()) != null) {
            ret.append(tmp);
        }
        return ret.toString();
    }

    @SneakyThrows
    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        String username = null;
        try {
            String requestParams = getRequestBodyString(request);
            LoginRequest loginRequest = objectMapper.readValue(requestParams, LoginRequest.class);
            username = loginRequest.getUsername();
            // 检查登录请求
            wedprUserService.checkWedprUserLoginReturn(loginRequest, userJwtConfig);
            // 解密前端加密传输过来的密码，给到security验证
            String password =
                    PasswordHelper.decryptPassword(
                            loginRequest.getPassword(), userJwtConfig.getPrivateKey());
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
        } catch (WeDPRException e) {
            logger.info("{} failed to login: ", username, e);
            updateAllowedTimeAndTryCount(username);
            authenticationExceptionAndReturn(response, e.getMessage());
            return null;
        } catch (Exception e) {
            logger.info("{} failed to login: ", username, e);
            updateAllowedTimeAndTryCount(username);
            authenticationExceptionAndReturn(response, "登录失败，请检查用户名和密码，核对验证码");
            return null;
        }
    }

    @SneakyThrows
    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authResult) {
        String wedprResponse = "";
        try {
            User user = (User) authResult.getPrincipal();
            UserToken userToken = this.userCache.getUserToken(user.getUsername());
            if (userToken == null) {
                throw new WeDPRException("The user " + user.getUsername() + " not registered!");
            }
            // 生成jwt
            HeaderInfo headerInfo = new HeaderInfo();
            TokenContents tokenContents = new TokenContents();
            tokenContents.addTokenContents(Constant.USER_TOKEN_CLAIM, userToken.serialize());
            String jwt =
                    TokenUtils.generateJWTToken(
                            headerInfo,
                            tokenContents,
                            this.userJwtConfig.getSecret(),
                            this.userJwtConfig.getExpireTime());
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setJwt(jwt);
            wedprResponse =
                    new WeDPRResponse(
                                    Constant.WEDPR_SUCCESS,
                                    Constant.WEDPR_SUCCESS_MSG,
                                    loginResponse)
                            .serialize();
            logger.info("{} login success, credential: {}", user.getUsername(), jwt);
            response.setStatus(HttpStatus.OK.value());
        } catch (Exception e) {
            logger.warn("生成jwt失败", e);
            wedprResponse = new WeDPRResponse(Constant.WEDPR_FAILED, "生成jwt失败").serialize();
        }
        TokenUtils.responseToClient(response, wedprResponse, HttpServletResponse.SC_OK);
    }

    @SneakyThrows
    @Override
    protected void unsuccessfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException failed) {
        UserToken userToken = TokenUtils.getLoginUser(request);
        updateAllowedTimeAndTryCount(userToken.getUsername());
        authenticationExceptionAndReturn(response, "认证失败，请检查用户名和密码");
    }

    /** 更新登录尝试次数 */
    private void updateAllowedTimeAndTryCount(String username) {
        WedprUser wedprUser =
                wedprUserService.getOne(
                        new LambdaQueryWrapper<WedprUser>().eq(WedprUser::getUsername, username));
        if (Objects.nonNull(wedprUser)) {
            Long allowedTimestamp = wedprUser.getAllowedTimestamp();
            int tryCount = wedprUser.getTryCount() + 1;
            if (tryCount >= userJwtConfig.getMaxTryCount()) {
                allowedTimestamp =
                        System.currentTimeMillis() + userJwtConfig.getLimitTimeThreshold();
            }
            wedprUserService.updateAllowedTimeAndTryCount(username, allowedTimestamp, tryCount);
        }
    }

    @SneakyThrows(Exception.class)
    private void authenticationExceptionAndReturn(HttpServletResponse response, String errorMessage)
            throws IOException {
        String wedprResponse = new WeDPRResponse(Constant.WEDPR_FAILED, errorMessage).serialize();
        TokenUtils.responseToClient(response, wedprResponse, HttpServletResponse.SC_OK);
    }
}
