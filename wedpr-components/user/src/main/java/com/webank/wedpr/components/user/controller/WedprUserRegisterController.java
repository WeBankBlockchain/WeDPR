package com.webank.wedpr.components.user.controller;

import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRResponse;
import com.webank.wedpr.components.user.config.UserJwtConfig;
import com.webank.wedpr.components.user.helper.TokenImageHelper;
import com.webank.wedpr.components.user.requests.UserRegisterRequest;
import com.webank.wedpr.components.user.response.WedprImageCodeResponse;
import com.webank.wedpr.components.user.response.WedprPublicKeyResponse;
import com.webank.wedpr.components.user.service.WedprGroupDetailService;
import com.webank.wedpr.components.user.service.WedprUserRoleService;
import com.webank.wedpr.components.user.service.WedprUserService;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
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
     * @param userRegisterRequest the register request
     * @return
     */
    @PostMapping("/register")
    @Transactional
    public WeDPRResponse register(@Valid @RequestBody UserRegisterRequest userRegisterRequest) {
        return wedprUserService.register(userRegisterRequest);
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
