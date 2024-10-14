package com.webank.wedpr.components.user.requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.webank.wedpr.components.user.entity.WedprUser;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = false)
public class AddGroupUserRequest {
    @NotEmpty(message = "新增用户不能为空")
    @Size(min = 1, message = "至少新增一个用户")
    @JsonProperty("userList")
    private List<WedprUser> wedprUserList;
}
