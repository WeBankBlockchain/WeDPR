package com.webank.wedpr.components.user.requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import lombok.Data;

/** Created by caryliao on 2024/7/18 16:58 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRegisterRequest {
    @Pattern(regexp = "^[a-zA-Z0-9_-]{3,18}$", message = "username format error")
    private String username;

    @NotBlank(message = "password is not be empty")
    private String password;

    @Pattern(regexp = "^[1]([3-9])[0-9]{9}$", message = "phone format error")
    private String phone;

    @Email(message = "email format error")
    private String email;
}
