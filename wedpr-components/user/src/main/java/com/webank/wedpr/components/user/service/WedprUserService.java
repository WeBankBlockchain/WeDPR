package com.webank.wedpr.components.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.user.config.UserJwtConfig;
import com.webank.wedpr.components.user.entity.WedprUser;
import com.webank.wedpr.components.user.requests.LoginRequest;
import com.webank.wedpr.components.user.requests.UserRegisterRequest;

/**
 * 服务类
 *
 * @author caryliao
 * @since 2024-07-15
 */
public interface WedprUserService extends IService<WedprUser> {
    void updateAllowedTimeAndTryCount(String username, Long allowedTime, Integer tryCount);

    /** 从username获取用户信息 */
    WedprUser getWedprUserByNameService(String username);

    /** 检查用户登录 */
    void checkWedprUserLoginReturn(LoginRequest request, UserJwtConfig userJwtConfig)
            throws WeDPRException;

    WeDPRResponse register(UserRegisterRequest userRegisterRequest);
}
