package com.webank.wedpr.components.user.helper;

import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.token.auth.model.UserToken;

public class PermissionHelper {

    public static void checkAdminRole(UserToken userToken) throws WeDPRException {
        if (!userToken.isAdmin()) {
            throw new WeDPRException("非管理员无法访问该接口");
        }
    }
}
