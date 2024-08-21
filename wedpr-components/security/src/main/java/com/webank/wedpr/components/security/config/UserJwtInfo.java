package com.webank.wedpr.components.security.config;

import com.webank.wedpr.components.token.auth.model.GroupInfo;
import java.util.List;
import lombok.Data;

/** Created by caryliao on 2024/8/12 22:21 */
@Data
public class UserJwtInfo {
    private String roleName;
    private List<GroupInfo> groupInfos;

    public UserJwtInfo() {}

    public UserJwtInfo(String roleName, List<GroupInfo> groupInfos) {
        this.roleName = roleName;
        this.groupInfos = groupInfos;
    }
}
