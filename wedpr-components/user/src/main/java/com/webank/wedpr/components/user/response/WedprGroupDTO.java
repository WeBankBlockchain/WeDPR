package com.webank.wedpr.components.user.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import lombok.Data;

/** Created by caryliao on 2024/8/5 19:30 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WedprGroupDTO {
    private String groupId;

    private String groupName;

    private String adminName;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String createBy;

    private String updateBy;

    private Integer status;

    private Integer userCount;
}
