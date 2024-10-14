package com.webank.wedpr.components.security.filter;

import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.security.cache.UserCache;
import com.webank.wedpr.components.token.auth.TokenUtils;
import com.webank.wedpr.components.token.auth.model.HeaderInfo;
import com.webank.wedpr.components.token.auth.model.TokenContents;
import com.webank.wedpr.components.token.auth.model.UserToken;
import com.webank.wedpr.components.token.auth.utils.SecurityUtils;
import com.webank.wedpr.components.user.config.UserJwtConfig;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.StringUtils;

public class JwtAuthenticationFilter extends BasicAuthenticationFilter {
    private final UserJwtConfig userJwtConfig;
    private final UserCache userCache;

    public JwtAuthenticationFilter(
            AuthenticationManager authenticationManager,
            UserJwtConfig userJwtConfig,
            UserCache userCache) {
        super(authenticationManager);
        this.userJwtConfig = userJwtConfig;
        this.userCache = userCache;
    }

    @SneakyThrows
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
        try {
            String jwt = request.getHeader(Constant.TOKEN_FIELD);
            if (StringUtils.isEmpty(jwt)) {
                logger.trace(
                        "With no jwt authentication information, try to call the APISignatureFilter");
                chain.doFilter(request, response);
                return;
            }
            TokenUtils.verify(SecurityUtils.HMAC_HS256, this.userJwtConfig.getSecret(), jwt);
            Pair<Boolean, UserToken> result = userCache.getUserToken(request);
            if (result == null) {
                throw new WeDPRException(Constant.WEDPR_FAILED, "用户不存在");
            }
            boolean updated = result.getKey();
            // the content not updated, use the original jwt
            if (!updated) {
                // set the token information into the response header
                response.setHeader(Constant.TOKEN_FIELD, jwt);
                chain.doFilter(request, response);
                return;
            }
            // the content updated, generate new jwt
            UserToken userToken = result.getRight();
            HeaderInfo headerInfo = new HeaderInfo();
            TokenContents tokenContents = new TokenContents();
            tokenContents.addTokenContents(Constant.USER_TOKEN_CLAIM, userToken.serialize());
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
            chain.doFilter(request, response);
        } catch (Exception e) {
            logger.info("jwt auth failed, error: ", e);
            String wedprResponse =
                    new WeDPRResponse(Constant.WEDPR_FAILED, "auth failed for " + e.getMessage())
                            .serialize();
            TokenUtils.responseToClient(response, wedprResponse, HttpServletResponse.SC_FORBIDDEN);
        }
    }
}
