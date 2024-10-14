package com.webank.wedpr.components.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webank.wedpr.common.protocol.UserRoleEnum;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.hook.UserHook;
import com.webank.wedpr.components.user.config.UserJwtConfig;
import com.webank.wedpr.components.user.entity.WedprGroupDetail;
import com.webank.wedpr.components.user.entity.WedprUser;
import com.webank.wedpr.components.user.entity.WedprUserRole;
import com.webank.wedpr.components.user.helper.PasswordHelper;
import com.webank.wedpr.components.user.helper.TokenImageHelper;
import com.webank.wedpr.components.user.mapper.WedprGroupDetailMapper;
import com.webank.wedpr.components.user.mapper.WedprUserMapper;
import com.webank.wedpr.components.user.mapper.WedprUserRoleMapper;
import com.webank.wedpr.components.user.requests.LoginRequest;
import com.webank.wedpr.components.user.requests.UserRegisterRequest;
import com.webank.wedpr.components.user.response.WedpRegisterResponse;
import com.webank.wedpr.components.user.service.WedprUserService;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.StringUtils;

/**
 * 服务实现类
 *
 * @author caryliao
 * @since 2024-07-15
 */
@Service
public class WedprUserImplService extends ServiceImpl<WedprUserMapper, WedprUser>
        implements WedprUserService {
    private static final Logger logger = LoggerFactory.getLogger(WedprUserImplService.class);

    @Autowired WedprUserRoleMapper wedprUserRoleMapper;
    @Autowired WedprGroupDetailMapper wedprGroupDetailMapper;
    @Autowired WedprUserMapper wedprUserMapper;
    @Autowired UserJwtConfig userJwtConfig;
    @Autowired UserHook userHook;

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
        String errorMessage;
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

    @Override
    public WeDPRResponse register(UserRegisterRequest userRegisterRequest) {
        try {
            // 检查用户名是否存在
            LambdaQueryWrapper<WedprUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            String username = userRegisterRequest.getUsername();
            lambdaQueryWrapper.eq(WedprUser::getUsername, username);
            WedprUser queriedWedprUser = getOne(lambdaQueryWrapper);
            if (queriedWedprUser != null) {
                return new WeDPRResponse(Constant.WEDPR_FAILED, "用户名已存在");
            }
            WedprUser wedprUser = getRegisterWedprUser(userRegisterRequest);
            WedprUserRole wedprUserRole =
                    WedprUserRole.builder()
                            .username(username)
                            .roleId(UserRoleEnum.ORIGINAL_USER.getRoleId())
                            .createBy(username)
                            .updateBy(username)
                            .build();
            wedprUserMapper.insert(wedprUser);
            wedprUserRoleMapper.insert(wedprUserRole);

            // 设置默认群组
            WedprGroupDetail wedprGroupDetail = new WedprGroupDetail();
            wedprGroupDetail.setGroupId(Constant.DEFAULT_INIT_GROUP_ID);
            wedprGroupDetail.setUsername(username);
            wedprGroupDetail.setCreateBy(username);
            wedprGroupDetail.setUpdateBy(username);
            wedprGroupDetailMapper.insert(wedprGroupDetail);
            // call the userHook
            this.userHook.onUserCreated(username);

            WeDPRResponse wedprResponse =
                    new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
            WedpRegisterResponse wedpRegisterResponse = new WedpRegisterResponse();
            wedpRegisterResponse.setUsername(wedprUser.getUsername());
            wedprResponse.setData(wedpRegisterResponse);
            return wedprResponse;
        } catch (Exception e) {
            logger.warn("注册用户失败", e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new WeDPRResponse(Constant.WEDPR_FAILED, "注册用户失败:" + e.getMessage());
        }
    }

    private WedprUser getRegisterWedprUser(UserRegisterRequest userRegisterRequest)
            throws WeDPRException {
        WedprUser wedprUser = new WedprUser();
        String username = userRegisterRequest.getUsername();
        wedprUser.setUsername(username);
        wedprUser.setTryCount(0);
        wedprUser.setCreateBy(username);
        wedprUser.setUpdateBy(username);
        String password = userRegisterRequest.getPassword();
        String plainPassword =
                PasswordHelper.decryptPassword(password, userJwtConfig.getPrivateKey());
        if (PasswordHelper.isStrongNonValid(plainPassword)) {
            throw new WeDPRException("注册密码至少包含8个字符，并且至少包含一个大写字母、一个小写字母、一个数字和一个特殊字符");
        }
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encodePassword =
                Constant.ENCRYPT_PREFIX + bCryptPasswordEncoder.encode(plainPassword);
        wedprUser.setPassword(encodePassword);
        String phone = userRegisterRequest.getPhone();
        if (StringUtils.hasLength(phone)) {
            wedprUser.setPhone(phone);
        }
        String email = userRegisterRequest.getEmail();
        if (StringUtils.hasLength(email)) {
            wedprUser.setEmail(email);
        }
        return wedprUser;
    }
}
