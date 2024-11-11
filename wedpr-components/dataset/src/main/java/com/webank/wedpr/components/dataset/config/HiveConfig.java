package com.webank.wedpr.components.dataset.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class HiveConfig {

    @Value("${wedpr.hive.jdbc.url:}")
    String hiveJdbcUrl;

    @Value("${wedpr.hive.user:}")
    String hiveUserName;

    @Value("${wedpr.hive.password:}")
    String hiveUserPassword;
}
