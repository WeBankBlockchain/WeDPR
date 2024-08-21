package com.webank.wedpr.components.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.LoadingCache;
import com.webank.wedpr.components.token.auth.TokenUtils;
import com.webank.wedpr.components.token.auth.model.GroupInfo;
import com.webank.wedpr.components.token.auth.model.HeaderInfo;
import com.webank.wedpr.components.token.auth.model.TokenContents;
import com.webank.wedpr.components.token.auth.model.UserToken;
import com.webank.wedpr.components.token.auth.utils.SecurityUtils;
import com.webank.wedpr.components.user.config.UserJwtConfig;
import com.webank.wedpr.components.user.entity.WedprUser;
import com.webank.wedpr.components.user.service.WedprUserService;
import com.webank.wedpr.core.utils.Constant;
import com.webank.wedpr.core.utils.ObjectMapperFactory;
import com.webank.wedpr.core.utils.WeDPRException;
import com.webank.wedpr.core.utils.WeDPRResponse;
import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.StringUtils;

public class JwtAuthenticationFilter extends BasicAuthenticationFilter {
    private static final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    private final UserJwtConfig userJwtConfig;
    private final WedprUserService wedprUserService;
    private final LoadingCache<String, UserJwtInfo> loadingCache;

    public JwtAuthenticationFilter(
            AuthenticationManager authenticationManager,
            UserJwtConfig userJwtConfig,
            WedprUserService wedprUserService,
            LoadingCache<String, UserJwtInfo> loadingCache) {
        super(authenticationManager);
        this.userJwtConfig = userJwtConfig;
        this.wedprUserService = wedprUserService;
        this.loadingCache = loadingCache;
    }

    @SneakyThrows
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
        try {
            String jwt = request.getHeader(Constant.TOKEN_FIELD);
            if (StringUtils.isEmpty(jwt)) {
                String wedprResponse =
                        new WeDPRResponse(Constant.WEDPR_FAILED, "未登录，请先登录").serialize();
                TokenUtils.responseToClient(
                        response, wedprResponse, HttpServletResponse.SC_FORBIDDEN);
                return;
            } else {
                TokenUtils.verify(SecurityUtils.HMAC_HS256, this.userJwtConfig.getSecret(), jwt);
            }
            UserToken userToken = TokenUtils.getLoginUser(request);
            String username = userToken.getUsername();
            WedprUser wedprUser = wedprUserService.getById(username);
            if (wedprUser != null) {
                UserJwtInfo userJwtInfo = loadingCache.get(username);
                String oldRoleName = userToken.getRoleName();
                List<GroupInfo> oldGroupInfos = userToken.getGroupInfos();
                boolean hasRoleNameUpdate = !oldRoleName.equals(userJwtInfo.getRoleName());
                boolean hasGroupInfoUpdate =
                        !CollectionUtils.isEqualCollection(
                                oldGroupInfos, userJwtInfo.getGroupInfos());
                if (hasRoleNameUpdate || hasGroupInfoUpdate) {
                    // 生成新的jwt token
                    UserToken newToken = new UserToken();
                    newToken.setUsername(username);
                    newToken.setRoleName(userJwtInfo.getRoleName());
                    newToken.setGroupInfos(userJwtInfo.getGroupInfos());
                    HeaderInfo headerInfo = new HeaderInfo();
                    TokenContents tokenContents = new TokenContents();
                    tokenContents.addTokenContents(Constant.USER_TOKEN_CLAIM, newToken.serialize());
                    String newJwt =
                            TokenUtils.generateJWTToken(
                                    headerInfo,
                                    tokenContents,
                                    this.userJwtConfig.getSecret(),
                                    this.userJwtConfig.getExpireTime());
                    HeaderMapRequestWrapper requestWrapper = new HeaderMapRequestWrapper(request);
                    requestWrapper.addHeader(Constant.TOKEN_FIELD, newJwt);
                    request = requestWrapper;
                    response.setHeader(Constant.TOKEN_FIELD, newJwt);
                }
            } else {
                throw new WeDPRException(Constant.WEDPR_FAILED, "用户不存在");
            }
            chain.doFilter(request, response);
        } catch (Exception e) {
            logger.info("认证已过期或token错误，请重新登录: ", e);
            String wedprResponse =
                    new WeDPRResponse(Constant.WEDPR_FAILED, "认证已过期或token错误，请重新登录").serialize();
            TokenUtils.responseToClient(response, wedprResponse, HttpServletResponse.SC_FORBIDDEN);
        }
    }
}
