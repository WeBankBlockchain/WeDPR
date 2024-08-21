package com.webank.wedpr.components.user.requests;

import javax.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/** Created by caryliao on 2024/7/18 16:58 */
@Data
public class UserRegisterRequest {
    @NotBlank(message = "用户名不能为空")
    @Length(max = 128, message = "用户名最多64个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @Length(max = 64, message = "电话最多64个字符")
    private String phone;

    @Length(max = 128, message = "电子邮箱最多128个字符")
    private String email;
}
