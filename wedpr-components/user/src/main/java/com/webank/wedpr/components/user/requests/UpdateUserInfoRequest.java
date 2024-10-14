package com.webank.wedpr.components.user.requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/** Created by caryliao on 2024/7/18 16:58 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateUserInfoRequest {
    @Length(max = 64, message = "电话最多64个字符")
    private String phone;

    @Length(max = 128, message = "电子邮箱最多128个字符")
    private String email;
}
