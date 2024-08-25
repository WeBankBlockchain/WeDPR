package com.webank.wedpr.components.mybatis;

import com.alibaba.druid.pool.DruidDataSource;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSourceConfig {
    private static final Logger logger = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driverClassName}")
    private String driverClassName;

    @Value("${spring.datasource.defaultAutoCommit}")
    private boolean defaultAutoCommit;

    @Value("${spring.datasource.initialSize}")
    private int initialSize;

    @Value("${spring.datasource.maxActive}")
    private int maxActive;

    @Value("${spring.datasource.maxIdle}")
    private int maxIdle;

    @Value("${spring.datasource.minIdle}")
    private int minIdle;

    @Value("${spring.datasource.queryTimeout}")
    private int queryTimeout = 0;

    @Value("${spring.datasource.keepAlive}")
    private boolean keepAlive;

    @Value("${spring.datasource.testOnBorrow}")
    private boolean testOnBorrow;

    @Value("${spring.datasource.testOnReturn}")
    private boolean testOnReturn;

    @Value("${spring.datasource.testWhileIdle}")
    private boolean testWhileIdle;

    @Value("${spring.datasource.maxWait}")
    private long maxWait;

    @Value("${spring.datasource.timeBetweenEvictionRunsMillis}")
    private long timeBetweenEvictionRunsMillis;

    @Value("${spring.datasource.minEvictableIdleTimeMillis}")
    private long minEvictableIdleTimeMillis;

    @Value("${spring.datasource.validationQuery}")
    private String validationQuery;

    @Value("${spring.datasource.validationQueryTimeout}")
    private int validationQueryTimeout;

    @Value("${spring.datasource.removeAbandoned}")
    private boolean removeAbandoned;

    @Value("${spring.datasource.logAbandoned}")
    private boolean logAbandoned;

    @Value("${spring.datasource.removeAbandonedTimeout}")
    private int removeAbandonedTimeout;

    @Value("${spring.datasource.poolPreparedStatements}")
    private boolean poolPreparedStatements;

    @Value("${spring.datasource.connectionProperties}")
    private String connectionProperties;

    @Value("${spring.datasource.filters}")
    private String filters;

    @Bean(name = "dataSource", destroyMethod = "close")
    public DruidDataSource dataSource() throws Exception {

        logger.info("dataSource dbUrl:{}", url);
        logger.info("dataSource dbUsername:{}", username);
        logger.info("dataSource driverClassName:{}", driverClassName);
        logger.info("dataSource queryTimeout:{}", queryTimeout);

        // https://github.com/alibaba/druid/wiki/DruidDataSource%E9%85%8D%E7%BD%AE

        DruidDataSource datasource = new DruidDataSource();
        datasource.setUrl(url);
        datasource.setUsername(username);
        datasource.setPassword(password);
        datasource.setDriverClassName(driverClassName);
        datasource.setDefaultAutoCommit(defaultAutoCommit);
        // configuration
        datasource.setInitialSize(initialSize);
        datasource.setMaxActive(maxActive);
        datasource.setMinIdle(maxIdle);
        datasource.setMinIdle(minIdle);
        datasource.setTestOnBorrow(testOnBorrow);
        datasource.setTestOnReturn(testOnReturn);
        datasource.setTestWhileIdle(testWhileIdle);
        datasource.setValidationQuery(validationQuery);
        datasource.setValidationQueryTimeout(validationQueryTimeout);
        datasource.setQueryTimeout(queryTimeout);
        //
        datasource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        datasource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        // datasource.setMaxEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        datasource.setKeepAlive(keepAlive);

        datasource.setRemoveAbandoned(removeAbandoned);
        datasource.setLogAbandoned(logAbandoned);
        datasource.setRemoveAbandonedTimeout(removeAbandonedTimeout);

        datasource.setMaxWait(maxWait);
        datasource.setPoolPreparedStatements(poolPreparedStatements);
        datasource.setConnectionProperties(connectionProperties);
        datasource.setFilters(filters);

        datasource.init();
        Map<String, Object> statData = datasource.getStatData();

        logger.info("create druid datasource successfully: {}", statData);

        return datasource;
    }
}
