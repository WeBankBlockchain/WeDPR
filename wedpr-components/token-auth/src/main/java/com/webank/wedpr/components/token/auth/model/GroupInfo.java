package com.webank.wedpr.components.token.auth.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** Created by caryliao on 2024/8/7 20:21 */
@Data
@EqualsAndHashCode
public class GroupInfo {
    private String groupId;
    private String groupName;
    private String groupAdminName;
}
