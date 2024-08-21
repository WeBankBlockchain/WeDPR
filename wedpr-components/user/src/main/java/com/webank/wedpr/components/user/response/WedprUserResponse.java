package com.webank.wedpr.components.user.response;

import java.time.LocalDateTime;
import lombok.Data;

/** Created by caryliao on 2024/7/18 16:58 */
@Data
public class WedprUserResponse {
    private String username;
    private String roleName;
    private String phone;
    private String email;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
