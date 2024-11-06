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

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.github.pagehelper.PageInterceptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@AutoConfigureAfter(DataSourceConfig.class)
@EnableTransactionManagement
public class MybatisConfigurationFactory {
    private static final Logger logger = LoggerFactory.getLogger(MybatisConfigurationFactory.class);

    @Autowired private DataSource dataSource;

    @Bean(name = "sqlSessionFactory")
    @Primary
    public MybatisSqlSessionFactoryBean sqlSessionFactory() {
        logger.info("create sqlSessionFactory, dataSource: {}", dataSource);
        String typeAliasesPackage = MybatisConfig.getMybatisTypeAliasesPackage();
        // Configure the mapper scan to find all mapper.xml mapping
        String mapperLocations = MybatisConfig.getMybatisMapperLocations();
        String configLocation = MybatisConfig.getMybatisConfigLocation();
        try {
            MybatisSqlSessionFactoryBean sessionFactoryBean = new MybatisSqlSessionFactoryBean();
            sessionFactoryBean.setDataSource(dataSource);
            logger.info(
                    "sqlSessionFactory, typeAliasesPackage: {}, mapperLocations: {}, configLocation: {}",
                    typeAliasesPackage,
                    mapperLocations,
                    configLocation);
            sessionFactoryBean.setTypeAliasesPackage(typeAliasesPackage);
            if (StringUtils.isNotBlank(mapperLocations)) {
                String[] mapperArray = mapperLocations.split(",");
                List<Resource> resources = new ArrayList<>();
                for (String mapperLocation : mapperArray) {
                    CollectionUtils.addAll(
                            resources,
                            new PathMatchingResourcePatternResolver().getResources(mapperLocation));
                }
                sessionFactoryBean.setMapperLocations(resources.toArray(new Resource[0]));
            }
            sessionFactoryBean.setConfigLocation(
                    new DefaultResourceLoader().getResource(configLocation));
            // Add paging plugin
            Interceptor[] plugins = new Interceptor[] {mybatisPlusInterceptor(), pageInterceptor()};
            sessionFactoryBean.setPlugins(plugins);
            logger.info("create sqlSessionFactory success");
            return sessionFactoryBean;
        } catch (IOException e) {
            logger.error("mybatis resolve mapper error: ", e);
            return null;
        } catch (Exception e) {
            logger.error("mybatis sqlSessionFactoryBean create error: ", e);
            return null;
        }
    }

    @Bean
    @Primary
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean
    @Primary
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public PageInterceptor pageInterceptor() {
        PageInterceptor pageInterceptor = new PageInterceptor();
        Properties properties = new Properties();
        properties.setProperty("reasonable", "true");
        properties.setProperty("pageSizeZero", "true");
        properties.setProperty("helperDialect", "mysql");
        pageInterceptor.setProperties(properties);
        return pageInterceptor;
    }

    @Bean
    @Primary
    public JdbcTemplate primaryJdbcTemplate() {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 如果配置多个插件, 切记分页最后添加
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        // 如果有多数据源可以不配具体类型, 否则都建议配上具体的 DbType
        return interceptor;
    }
}
