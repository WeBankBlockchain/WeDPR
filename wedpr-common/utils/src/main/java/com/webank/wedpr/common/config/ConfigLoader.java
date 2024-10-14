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

package com.webank.wedpr.common.config;

import com.webank.wedpr.common.utils.PropertiesHelper;
import com.webank.wedpr.common.utils.WeDPRException;
import java.io.InputStream;
import java.util.Properties;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: support hot-reload
public class ConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
    // the default wedpr config
    private static final String DEFAULT_CONFIG_FILE_NAME = "wedpr.properties";
    private static final String CONFIG_PATH_PROPERTY = "wedpr.config.path";

    @SneakyThrows
    public static Properties loadConfig(String configFile) {
        if (configFile == null) {
            configFile =
                    PropertiesHelper.getValue(
                            System.getProperties(),
                            CONFIG_PATH_PROPERTY,
                            false,
                            DEFAULT_CONFIG_FILE_NAME);
        }
        logger.info(
                "************ Notice: The WeDPR configuration file is: {} ************",
                configFile);
        try (InputStream configStream =
                ConfigLoader.class.getClassLoader().getResourceAsStream(configFile)) {
            Properties config = new Properties();
            config.load(configStream);
            logger.info(
                    "************ Load WeDPR configuration: {} success ************", configFile);
            return config;

        } catch (Exception e) {
            throw new WeDPRException(
                    "ConfigLoader: load config from "
                            + configFile
                            + " failed, error: "
                            + e.getMessage(),
                    e);
        }
    }
}
