package com.webank.wedpr.components.user.requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.validation.constraints.NotBlank;
import lombok.Data;

/** Created by caryliao on 2024/7/18 16:58 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdatePasswordRequest {
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
