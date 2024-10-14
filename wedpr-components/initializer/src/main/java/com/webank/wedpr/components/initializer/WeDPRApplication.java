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

package com.webank.wedpr.components.initializer;

import com.webank.wedpr.common.config.WeDPRConfig;
import com.webank.wedpr.common.utils.WeDPRException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.webank"})
public class WeDPRApplication extends SpringBootServletInitializer {
    private static final Logger logger = LoggerFactory.getLogger(WeDPRApplication.class);
    private static final String SPRING_CONFIGURATION = "spring.";

    protected static ServiceInfo serviceInfo;
    protected static ConfigurableApplicationContext applicationContext;

    public static void main(String[] args, String serviceName) throws Exception {
        final SpringApplication application = new SpringApplication(WeDPRApplication.class);
        application.addListeners(
                new ApplicationListener<ApplicationPreparedEvent>() {
                    @Override
                    public void onApplicationEvent(ApplicationPreparedEvent event) {
                        logger.info("init WeDPRApplication application");
                        if (applicationContext == null) {
                            applicationContext = event.getApplicationContext();
                        }
                        initWeDPRApplication(serviceName);
                        logger.info("init WeDPRApplication application success");
                    }
                });
        String[] springArgs = generateSpringArgs();
        applicationContext = application.run(ArrayUtils.addAll(args, springArgs));
    }

    protected static void initWeDPRApplication(String serviceName) {
        try {
            serviceInfo = new ServiceInfo();
            String appName =
                    applicationContext.getEnvironment().getProperty("spring.application.name");
            if (StringUtils.isBlank(appName)) {
                appName = serviceName;
            }
            logger.info("initWeDPRApplication for {}", appName);
            // set the application
            serviceInfo.setApplicationName(appName);
            // set the instance
            String configFile =
                    applicationContext
                            .getEnvironment()
                            .getProperty("spring.application.config.file");
            if (!StringUtils.isBlank(configFile)) {
                serviceInfo.setConfigFile(configFile);
            }
            // load config
            loadConfig(serviceInfo.getConfigFile());

            logger.info("initWeDPRApplication for {} success", appName);
        } catch (Exception e) {
            logger.error("initWeDPRApplication failed, error: ", e);
            System.exit(-1);
        }
    }

    private static void loadConfig(String configFile) throws Exception {
        logger.info(
                "====== Begin to load config file from: {} ========", serviceInfo.getConfigFile());
        InputStream configStream =
                WeDPRApplication.class.getClassLoader().getResourceAsStream(configFile);
        if (configStream == null) {
            throw new WeDPRException(
                    "loadConfig from " + configFile + " failed for obtain empty config!");
        }
        Properties configProperties = new Properties();
        configProperties.load(configStream);
        WeDPRConfig.setConfig(configProperties);
        logger.info(
                "====== Load config file from {} success ========", serviceInfo.getConfigFile());
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(WeDPRApplication.class);
    }

    public static ServiceInfo getServiceInfo() {
        return serviceInfo;
    }

    private static String[] generateSpringArgs() {
        // WeDPRConfig.getConfig().keySet()
        List<String> springArgs = new ArrayList<>();
        springArgs.add("--spring.profiles.active=wedpr");
        for (String key : WeDPRConfig.getConfig().stringPropertyNames()) {
            if (key.startsWith(SPRING_CONFIGURATION)) {
                String springOption =
                        "--"
                                + key.substring(SPRING_CONFIGURATION.length())
                                + "="
                                + WeDPRConfig.getConfig().getProperty(key);
                springArgs.add(springOption);
            }
        }
        String[] springArgsArray = new String[springArgs.size()];
        springArgs.toArray(springArgsArray);
        return springArgsArray;
    }

    public static ConfigurableApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
