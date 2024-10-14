/*
 * Copyright 2017-2025  [webank-wedpr]
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.webank.wedpr.components.mybatis;

import com.webank.wedpr.common.config.WeDPRConfig;
import java.io.InputStream;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MybatisConfig {
    private static final Logger logger = LoggerFactory.getLogger(MybatisConfig.class);
    public static final String WEDPR_MYBATIS_DATASOURCE_CONFIG_PATH =
            WeDPRConfig.apply("wedpr.mybatis.config.path", "");
    public static final String WEDPR_MYBATIS_DATASOURCE_URL =
            WeDPRConfig.apply("wedpr.mybatis.url", "");
    public static final String WEDPR_MYBATIS_USER_NAME =
            WeDPRConfig.apply("wedpr.mybatis.username", "");
    public static final String WEDPR_MYBATIS_PASSWORD =
            WeDPRConfig.apply("wedpr.mybatis.password", "");

    public static final String WEDPR_MYBATIS_DRIVER_CLASS =
            WeDPRConfig.apply("wedpr.mybatis.driverClassName", "com.mysql.cj.jdbc.Driver");
    public static final Integer WEDPR_MYBATIS_DATASOURCE_INITIALIZE =
            WeDPRConfig.apply("wedpr.mybatis.datasource.initialize.size", 1);
    public static final Integer WEDPR_MYBATIS_MINIDLE =
            WeDPRConfig.apply("wedpr.mybatis.datasource.minIdle", 1);
    public static final Integer WEDPR_MYBATIS_MAXACTIVE =
            WeDPRConfig.apply("wedpr.mybatis.datasource.maxActive", 20);

    public static final Integer WEDPR_MYBATIS_MAXWAIT =
            WeDPRConfig.apply("wedpr.mybatis.datasource.maxWait", 6000);
    public static final Integer WEDPR_MYBATIS_TIME_BETWEEN_EVICTION =
            WeDPRConfig.apply("wedpr.mybatis.datasource.timeBetweenEvictionRunsMillis", 60000);
    public static final Integer WEDPR_MYBATIS_MIN_EVICT_IDLE =
            WeDPRConfig.apply("wedpr.mybatis.datasource.minEvictableIdleTimeMillis", 300000);

    // sql to validate the connection
    public static final String WEDPR_MYBATIS_VALIDATION_QUERY =
            WeDPRConfig.apply("wedpr.mybatis.validationQuery", "select 1");
    // if the idle time larger than timeBetweenEvictionRunsMillis, execute validationQuery to check
    // the connection
    public static final Boolean WEDPR_MYBATIS_TEST_WHILE_IDLE =
            WeDPRConfig.apply("wedpr.mybatis.testWhileIdle", Boolean.TRUE);
    // if set true, validationQuery will be executed when apply for new connection
    public static final Boolean WEDPR_MYBATIS_TEST_ON_BORROW =
            WeDPRConfig.apply("wedpr.mybatis.testOnBorrow", Boolean.FALSE);
    // if set true, validationQuery will be executed when return the connection
    public static final Boolean WEDPR_MYBATIS_TEST_ON_RETURN =
            WeDPRConfig.apply("wedpr.mybatis.testOnReturn", Boolean.FALSE);
    // cache the preparedStatement or not
    public static final Boolean WEDPR_MYBATIS_POOL_PREPARE_STATEMENTS =
            WeDPRConfig.apply("wedpr.mybatis.poolPreparedStatements", Boolean.FALSE);

    public static final Boolean WEDPR_MYBATIS_KEEPALIVE_ENABLED =
            WeDPRConfig.apply("wedpr.mybatis.keepalive.enabled", Boolean.TRUE);
    public static final Boolean WEDPR_MYBATIS_REMOVE_ABANDONED_ENABLE =
            WeDPRConfig.apply("wedpr.mybatis.remove.abandoned.enabled", Boolean.TRUE);
    public static final Integer WEDPR_MYBATIS_REMOVE_ABANDONED_TIMEOUT =
            WeDPRConfig.apply("wedpr.mybatis.remove.abandoned.timeout.ms", 300000);

    public static Properties MYBATIS_CONFIG = null;

    // the mybatis configuration
    private static final String MYBATIS_MAPPERLOCATIONS =
            WeDPRConfig.apply("wedpr.mybatis.mapperLocations", "");
    private static final String MYBATIS_TYPEALIASESPACKAGE =
            WeDPRConfig.apply("wedpr.mybatis.typeAliasesPackage", "");
    private static final String MYBATIS_CONFIGLOCATION =
            WeDPRConfig.apply("wedpr.mybatis.configLocation", "classpath:mybatis-config.xml");
    private static final String MYBATIS_BASEPACKAGE =
            WeDPRConfig.apply("wedpr.mybatis.BasePackage", "");
    public static final String MYBATIS_FILTERS = WeDPRConfig.apply("wedpr.mybatis.filters", null);
    public static final String MYBATIS_CONNECTION_PROPERTIES =
            WeDPRConfig.apply("wedpr.mybatis.connection.properties", null);

    static {
        // load configuration from the config file is the file exists
        if (!StringUtils.isBlank(WEDPR_MYBATIS_DATASOURCE_CONFIG_PATH)) {
            logger.info("Initialize mybatis config from {}", WEDPR_MYBATIS_DATASOURCE_CONFIG_PATH);
            try (InputStream configStream =
                    MybatisConfig.class
                            .getClassLoader()
                            .getResourceAsStream(WEDPR_MYBATIS_DATASOURCE_CONFIG_PATH)) {
                MYBATIS_CONFIG = new Properties();
                MYBATIS_CONFIG.load(configStream);
                logger.info(
                        "Initialize mybatis config from {} success",
                        WEDPR_MYBATIS_DATASOURCE_CONFIG_PATH);
            } catch (Exception e) {
                logger.warn(
                        "Initialize mybatis config from {} failed, use default configuration, error: ",
                        WEDPR_MYBATIS_DATASOURCE_CONFIG_PATH,
                        e);
                MYBATIS_CONFIG = null;
            }
        }
    }

    public static String getMybatisMapperLocations() {
        return MYBATIS_MAPPERLOCATIONS;
    }

    public static String getMybatisTypeAliasesPackage() {
        return MYBATIS_TYPEALIASESPACKAGE;
    }

    public static String getMybatisConfigLocation() {
        return MYBATIS_CONFIGLOCATION;
    }

    public static String getMybatisBasePackage() {
        return MYBATIS_BASEPACKAGE;
    }
}
