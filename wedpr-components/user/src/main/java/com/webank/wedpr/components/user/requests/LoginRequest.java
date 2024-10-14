package com.webank.wedpr.components.user.requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginRequest {
    private String username;
    private String password;
    private String randomToken;
    private String imageCode;
    private String mailCode;
}
