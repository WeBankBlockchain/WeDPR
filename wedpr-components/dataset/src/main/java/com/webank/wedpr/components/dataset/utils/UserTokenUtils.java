package com.webank.wedpr.components.dataset.utils;

import com.webank.wedpr.components.dataset.config.DatasetConfig;
import com.webank.wedpr.components.dataset.dao.UserInfo;
import com.webank.wedpr.components.token.auth.TokenUtils;
import com.webank.wedpr.components.token.auth.model.GroupInfo;
import com.webank.wedpr.components.token.auth.model.UserToken;
import com.webank.wedpr.core.config.WeDPRCommonConfig;
import com.webank.wedpr.core.utils.WeDPRException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserTokenUtils {

    private static final Logger logger = LoggerFactory.getLogger(UserTokenUtils.class);

    private UserTokenUtils() {}

    public static UserInfo getUserInfo(
            DatasetConfig datasetConfig, HttpServletRequest httpServletRequest)
            throws WeDPRException {

        try {
            // decode login user info
            UserToken userToken = TokenUtils.getLoginUser(httpServletRequest);

            String agency = WeDPRCommonConfig.getAgency();
            UserInfo userInfo =
                    UserInfo.builder()
                            .role(userToken.getRoleName())
                            .user(userToken.getUsername())
                            .groupInfos(userToken.getGroupInfos())
                            .agency(agency)
                            .build();

            logger.info(" get user info from token, agency: {}, user token: {}", agency, userToken);
            return userInfo;
        } catch (Exception e) {
            if (!datasetConfig.isDebugModel()) {
                throw e;
            }
        }

        // Note: test model
        String tokenField = datasetConfig.getDebugModelUserTokenField();

        String token = httpServletRequest.getHeader(tokenField);

        // role|agency|userGroup|user
        String[] splitStrings = token.split("\\|");

        List<GroupInfo> groupInfoList = new ArrayList<>();
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setGroupId(splitStrings[2]);
        groupInfo.setGroupName(splitStrings[2]);
        groupInfoList.add(groupInfo);

        UserInfo userInfo =
                UserInfo.builder()
                        .role(splitStrings[0])
                        .agency(splitStrings[1])
                        .groupInfos(groupInfoList)
                        .user(splitStrings[3])
                        .build();

        logger.info("debug model, try to solve use info from test field, user: {}", userInfo);
        return userInfo;
    }
}
