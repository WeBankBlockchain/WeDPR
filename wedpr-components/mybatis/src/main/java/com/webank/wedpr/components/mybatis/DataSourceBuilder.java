/*
 * Copyright 2017-2025 [webank-wedpr]
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 */

package com.webank.wedpr.components.mybatis;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.webank.wedpr.common.utils.WeDPRException;
import java.util.Map;
import java.util.Properties;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSourceBuilder {
    private static final Logger logger = LoggerFactory.getLogger(DataSourceBuilder.class);

    @SneakyThrows(Exception.class)
    public static DataSource build(Properties dataSourceConfig) {
        try {
            return DruidDataSourceFactory.createDataSource(dataSourceConfig);
        } catch (Exception e) {
            logger.error("Build dataSource failed, error: ", e);
            throw new WeDPRException(
                    "Build Datasource from config property failed, error : " + e.getMessage(), e);
        }
    }

    public static DataSource build() throws Exception {
        logger.info(
                "Begin Build DataSource, url: {}, user: {}",
                MybatisConfig.WEDPR_MYBATIS_DATASOURCE_URL,
                MybatisConfig.WEDPR_MYBATIS_USER_NAME);
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(MybatisConfig.WEDPR_MYBATIS_DATASOURCE_URL);
        dataSource.setUsername(MybatisConfig.WEDPR_MYBATIS_USER_NAME);
        dataSource.setPassword(MybatisConfig.WEDPR_MYBATIS_PASSWORD);
        dataSource.setDriverClassName(MybatisConfig.WEDPR_MYBATIS_DRIVER_CLASS);
        dataSource.setInitialSize(MybatisConfig.WEDPR_MYBATIS_DATASOURCE_INITIALIZE);
        dataSource.setMinIdle(MybatisConfig.WEDPR_MYBATIS_MINIDLE);
        dataSource.setMaxActive(MybatisConfig.WEDPR_MYBATIS_MAXACTIVE);
        dataSource.setMaxWait(MybatisConfig.WEDPR_MYBATIS_MAXWAIT);
        dataSource.setTimeBetweenEvictionRunsMillis(
                MybatisConfig.WEDPR_MYBATIS_TIME_BETWEEN_EVICTION);
        dataSource.setMinEvictableIdleTimeMillis(MybatisConfig.WEDPR_MYBATIS_MIN_EVICT_IDLE);
        dataSource.setValidationQuery(MybatisConfig.WEDPR_MYBATIS_VALIDATION_QUERY);
        dataSource.setTestWhileIdle(MybatisConfig.WEDPR_MYBATIS_TEST_WHILE_IDLE);
        dataSource.setTestOnBorrow(MybatisConfig.WEDPR_MYBATIS_TEST_ON_BORROW);
        dataSource.setTestOnReturn(MybatisConfig.WEDPR_MYBATIS_TEST_ON_RETURN);
        dataSource.setKeepAlive(MybatisConfig.WEDPR_MYBATIS_KEEPALIVE_ENABLED);
        dataSource.setPoolPreparedStatements(MybatisConfig.WEDPR_MYBATIS_POOL_PREPARE_STATEMENTS);
        dataSource.setRemoveAbandoned(MybatisConfig.WEDPR_MYBATIS_REMOVE_ABANDONED_ENABLE);
        dataSource.setRemoveAbandonedTimeoutMillis(
                MybatisConfig.WEDPR_MYBATIS_REMOVE_ABANDONED_TIMEOUT);
        if (MybatisConfig.MYBATIS_CONNECTION_PROPERTIES != null) {
            dataSource.setConnectionProperties(MybatisConfig.MYBATIS_CONNECTION_PROPERTIES);
        }
        if (MybatisConfig.MYBATIS_FILTERS != null) {
            dataSource.setFilters(MybatisConfig.MYBATIS_FILTERS);
        }
        dataSource.init();
        Map<String, Object> statData = dataSource.getStatData();
        logger.info(
                "Build DataSource success, url: {}, user: {}, statData: {}",
                MybatisConfig.WEDPR_MYBATIS_DATASOURCE_URL,
                MybatisConfig.WEDPR_MYBATIS_USER_NAME,
                statData);
        return dataSource;
    }
}
