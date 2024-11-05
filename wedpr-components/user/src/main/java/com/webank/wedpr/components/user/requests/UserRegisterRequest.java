package com.webank.wedpr.components.user.requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.validation.constraints.NotBlank;
import lombok.Data;

/** Created by caryliao on 2024/7/18 16:58 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRegisterRequest {
    @NotBlank(message = "username is not be empty")
    private String username;

    @NotBlank(message = "password is not be empty")
    private String password;

    @NotBlank(message = "phone is not be empty")
    private String phone;

    @NotBlank(message = "email is not be empty")
    private String email;
}
