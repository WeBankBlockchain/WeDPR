package com.webank.wedpr.components.user.requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/** Created by caryliao on 2024/7/18 16:58 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateWedprGroupRequest {
    @NotBlank(message = "用户组名称不能为空")
    @Length(max = 18, message = "用户组名称最多64个字符")
    private String groupName;
}
