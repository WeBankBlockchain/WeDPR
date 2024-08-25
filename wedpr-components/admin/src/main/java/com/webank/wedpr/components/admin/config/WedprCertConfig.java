package com.webank.wedpr.components.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "wedpr.cert")
@Data
public class WedprCertConfig {
    private String certScriptDir;
    private String certScript;
    private String rootCertPath;
    private String agencyCertPath;
}
