package com.webank.wedpr.components.user.requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** Created by caryliao on 2024/7/18 14:07 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = false)
public class CreateAdminUserRequest {
    @NotBlank(message = "用户组id不能为空")
    private String groupId;

    @NotBlank(message = "用户名不能为空")
    private String username;
}
