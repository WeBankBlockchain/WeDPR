package com.webank.wedpr.components.db.mapper.dataset.dao;

import com.webank.wedpr.components.token.auth.model.GroupInfo;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfo {
    private String role;
    private String user;
    private String agency;
    private List<GroupInfo> groupInfos;

    public List<String> getUserGroupList() {
        List<String> userGroupList = new ArrayList<>();
        if (groupInfos != null) {
            for (GroupInfo groupInfo : groupInfos) {
                userGroupList.add(groupInfo.getGroupId());
            }
        }

        return userGroupList;
    }
}
