package com.webank.wedpr.components.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.webank.wedpr.components.user.config.UserJwtConfig;
import com.webank.wedpr.components.user.entity.WedprGroupDetail;
import com.webank.wedpr.components.user.entity.WedprUser;
import com.webank.wedpr.components.user.entity.WedprUserRole;
import com.webank.wedpr.components.user.helper.PasswordHelper;
import com.webank.wedpr.components.user.helper.TokenImageHelper;
import com.webank.wedpr.components.user.requests.UserRegisterRequest;
import com.webank.wedpr.components.user.response.WedpRegisterResponse;
import com.webank.wedpr.components.user.response.WedprImageCodeResponse;
import com.webank.wedpr.components.user.response.WedprPublicKeyResponse;
import com.webank.wedpr.components.user.service.WedprGroupDetailService;
import com.webank.wedpr.components.user.service.WedprUserRoleService;
import com.webank.wedpr.components.user.service.WedprUserService;
import com.webank.wedpr.core.protocol.UserRoleEnum;
import com.webank.wedpr.core.utils.Constant;
import com.webank.wedpr.core.utils.WeDPRException;
import com.webank.wedpr.core.utils.WeDPRResponse;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前端控制器
 *
 * @author caryliao
 * @since 2024-07-15
 */
@RestController
@RequestMapping(
        path = Constant.WEDPR_API_PREFIX,
        produces = {"application/json"})
@Slf4j
public class WedprUserRegisterController {

    @Autowired private WedprUserService wedprUserService;

    @Autowired private WedprUserRoleService wedprUserRoleService;

    @Autowired private WedprGroupDetailService wedprGroupDetailService;

    @Autowired private UserJwtConfig userJwtConfig;

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    @Transactional
    public WeDPRResponse register(@Valid @RequestBody UserRegisterRequest userRegisterRequest) {
        try {
            // 检查用户名是否存在
            LambdaQueryWrapper<WedprUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            String username = userRegisterRequest.getUsername();
            lambdaQueryWrapper.eq(WedprUser::getUsername, username);
            WedprUser queriedWedprUser = wedprUserService.getOne(lambdaQueryWrapper);
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
            wedprUserService.save(wedprUser);
            wedprUserRoleService.save(wedprUserRole);

            // 设置默认群组
            WedprGroupDetail wedprGroupDetail = new WedprGroupDetail();
            wedprGroupDetail.setGroupId(Constant.DEFAULT_INIT_GROUP_ID);
            wedprGroupDetail.setUsername(username);
            wedprGroupDetail.setCreateBy(username);
            wedprGroupDetail.setUpdateBy(username);
            wedprGroupDetailService.save(wedprGroupDetail);

            WeDPRResponse wedprResponse =
                    new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
            WedpRegisterResponse wedpRegisterResponse = new WedpRegisterResponse();
            wedpRegisterResponse.setUsername(wedprUser.getUsername());
            wedprResponse.setData(wedpRegisterResponse);
            return wedprResponse;
        } catch (Exception e) {
            log.warn("注册用户失败", e);
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

    @GetMapping("/pub")
    public WeDPRResponse publicKeyController() {
        String publicKey = userJwtConfig.getPublicKey();
        if (!StringUtils.startsWithIgnoreCase(publicKey, "04")) {
            publicKey = "04" + publicKey;
        }
        return new WeDPRResponse(
                Constant.WEDPR_SUCCESS,
                Constant.WEDPR_SUCCESS_MSG,
                new WedprPublicKeyResponse(publicKey));
    }

    @GetMapping("/image-code")
    public WeDPRResponse imageCodeController() {
        try {
            String randomCode = TokenImageHelper.imageRandomString(userJwtConfig.getCodeLength());
            String imageBase64 = TokenImageHelper.getBase64Image(randomCode);
            String randomToken =
                    TokenImageHelper.generateImageSessionToken(randomCode, userJwtConfig);
            return new WeDPRResponse(
                    Constant.WEDPR_SUCCESS,
                    Constant.WEDPR_SUCCESS_MSG,
                    new WedprImageCodeResponse(randomToken, imageBase64));
        } catch (Exception e) {
            return new WeDPRResponse(Constant.WEDPR_FAILED, "验证码生成失败");
        }
    }
}
