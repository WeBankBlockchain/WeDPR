package com.webank.wedpr.components.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webank.wedpr.components.user.config.UserJwtConfig;
import com.webank.wedpr.components.user.entity.WedprUser;
import com.webank.wedpr.components.user.helper.TokenImageHelper;
import com.webank.wedpr.components.user.mapper.WedprUserMapper;
import com.webank.wedpr.components.user.requests.LoginRequest;
import com.webank.wedpr.components.user.service.WedprUserService;
import com.webank.wedpr.core.utils.WeDPRException;
import java.util.Objects;
import org.springframework.stereotype.Service;

/**
 * 服务实现类
 *
 * @author caryliao
 * @since 2024-07-15
 */
@Service
public class WedprUserImplService extends ServiceImpl<WedprUserMapper, WedprUser>
        implements WedprUserService {
    @Override
    public void updateAllowedTimeAndTryCount(String username, Long allowedTime, Integer tryCount) {
        baseMapper.updateAllowedTimeAndTryCount(username, allowedTime, tryCount);
    }

    @Override
    public WedprUser getWedprUserByNameService(String username) {
        LambdaQueryWrapper<WedprUser> lambdaUserNameQueryWrapper =
                new LambdaQueryWrapper<WedprUser>().eq(WedprUser::getUsername, username);
        return this.getOne(lambdaUserNameQueryWrapper);
    }

    @Override
    public void checkWedprUserLoginReturn(LoginRequest loginRequest, UserJwtConfig userJwtConfig)
            throws WeDPRException {
        String errorMessage = "";
        String username = loginRequest.getUsername();
        WedprUser wedprUser =
                this.getOne(
                        new LambdaQueryWrapper<WedprUser>().eq(WedprUser::getUsername, username));
        if (Objects.isNull(wedprUser)) {
            errorMessage = "用户不存在";
            throw new WeDPRException(errorMessage);
        }
        // 是否超验证次数等待
        Long allowedTime = wedprUser.getAllowedTimestamp();
        Long now = System.currentTimeMillis();
        if (now < allowedTime) {
            errorMessage =
                    String.format("请在 %d(min) 后登录", ((allowedTime - now) / (60 * 1000) + 1L));
            throw new WeDPRException(errorMessage);
        }

        // 检查用户状态
        if (wedprUser.getStatus() == 1) {
            errorMessage = "用户暂时无法使用，请联系管理员";
            throw new WeDPRException(errorMessage);
        }

        // 检查imagecode
        TokenImageHelper.checkImageLoginToken(
                loginRequest.getRandomToken(), loginRequest.getImageCode(), userJwtConfig);
    }
}
